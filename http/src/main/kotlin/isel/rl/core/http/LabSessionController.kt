package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.sseEmitter.SseEmitterBasedEventEmitter
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.ILabSessionService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.TimeUnit

@RestController
data class LabSessionController(
    private val labSessionService: ILabSessionService,
) {
    @GetMapping(Uris.LabSession.CREATE)
    fun createLabSession(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestParam listen: String?,
    ): Any {
        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))
        val eventEmitter = SseEmitterBasedEventEmitter(sseEmitter)

        // Validate lab session creation
        return when (val result =
            labSessionService.handleLabSessionCreation(labId = id, userId = user.user.id, eventEmitter)) {
            is Success -> sseEmitter
            is Failure -> handleServicesExceptions(result.value)
        }
    }

    /*private fun createSseConnection(labSession: LabSession): SseEmitter {
        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))

        try {
            // Send initial event
            sseEmitter.send(
                SseEmitter.event()
                    .id("session-created-${labSession.id}")
                    .name("labSessionCreated")
                    .data(LabSessionOutputModel.mapOf(labSession)),
            )

            // Start the lab session monitoring in a separate coroutine
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    labSessionService
                        .listenToChannel("teste")
                        .first()
                        .let { message ->
                            sseEmitter.send(
                                SseEmitter.event()
                                    .name("redisMessage")
                                    .data(message)
                            )

                            /*labSessionService.startLabSession(
                                SseEmitterBasedEventEmitter(sseEmitter),
                                labSession
                            )*/
                        }
                } catch (e: Exception) {
                    try {
                        sseEmitter.completeWithError(e)
                    } catch (_: Exception) {
                    }
                }
                /*
                try {
                    labSessionService.startLabSession(
                        SseEmitterBasedEventEmitter(sseEmitter),
                        labSession,
                    )
                } catch (e: Exception) {
                    try {
                        sseEmitter.completeWithError(e)
                    } catch (_: Exception) {
                    }
                }

     */
            }
        } catch (e: Exception) {
            sseEmitter.completeWithError(e)
        }

        return sseEmitter
    }

     */
}
