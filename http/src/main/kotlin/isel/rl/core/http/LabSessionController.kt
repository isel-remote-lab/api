package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.labSession.LabSessionOutputModel
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.sseEmitter.SseEmitterBasedEventEmitter
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.ILabSessionService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.TimeUnit

@RestController
data class LabSessionController(
    private val labSessionService: ILabSessionService,
) {
    @PostMapping(Uris.LabSession.CREATE)
    suspend fun createLabSession(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestParam listen: String?,
    ): Any =
        when (val result = labSessionService.createLabSession(labId = id, userId = user.user.id)) {
            is Success -> {
                if (listen.toBoolean()) {
                    createSseConnection(id.toInt(), result.value)
                } else {
                    ResponseEntity.status(HttpStatus.CREATED).body(
                        SuccessResponse(
                            message = "Lab session created successfully",
                            data = LabSessionOutputModel.mapOf(result.value),
                        ),
                    )
                }
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    private fun createSseConnection(
        labId: Int,
        labSession: LabSession,
    ): SseEmitter {
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
            // Use GlobalScope or application-scoped coroutine scope
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    labSessionService.startLabSession(
                        SseEmitterBasedEventEmitter(sseEmitter),
                        labId = labId,
                        labSessionId = labSession.id,
                    )
                } catch (e: Exception) {
                    try {
                        sseEmitter.completeWithError(e)
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (e: Exception) {
            sseEmitter.completeWithError(e)
        }

        return sseEmitter
    }
}
