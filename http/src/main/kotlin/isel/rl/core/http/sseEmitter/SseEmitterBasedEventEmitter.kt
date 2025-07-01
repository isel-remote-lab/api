package isel.rl.core.http.sseEmitter

import EventEmitter
import isel.rl.core.domain.events.Event
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

/**
 * SseEmitterBasedEventEmitter class that implements the EventEmitter interface using a SseEmitter.
 *
 * @property sseEmitter the SseEmitter
 */
class SseEmitterBasedEventEmitter(
    private val sseEmitter: SseEmitter,
) : EventEmitter {
    @Volatile
    private var isDisconnected = false

    override fun emit(event: Event) {
        if (isDisconnected) {
            LOG.debug("Client disconnected, skipping event emission")
            return
        }

        try {
            val sseEvent =
                when (event) {
                    is Event.LabSessionState ->
                        SseEmitter.event()
                            .id(event.eventId.toString())
                            .name("message")
                            .data(event)

                    is Event.LabSessionStarting ->
                        SseEmitter.event()
                            .id(event.eventId.toString())
                            .name("labSessionStarting")
                            .data(event)

                    is Event.Error -> SseEmitter.event()

                    is Event.KeepAlive ->
                        SseEmitter.event()
                            .comment(event.timestamp.epochSeconds.toString())
                }
            sseEmitter.send(sseEvent)
        } catch (e: IOException) {
            LOG.warn("Client disconnected while sending SSE event: ${e.message}")
            isDisconnected = true

            try {
                sseEmitter.complete()
            } catch (ex: Exception) {
                LOG.debug("Error completing SSE emitter: ${ex.message}")
            }
        }
    }

    override fun complete() {
        if (!isDisconnected) {
            try {
                sseEmitter.complete()
            } catch (e: IOException) {
                LOG.debug("Error completing SSE emitter: ${e.message}")
            }
        }
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError { throwable ->
            if (throwable is IOException) {
                LOG.warn("SSE connection error: ${throwable.message}")
                isDisconnected = true
            }
            callback(throwable)
        }
    }

    override fun onTimeout(callback: () -> Unit) {
        sseEmitter.onTimeout {
            LOG.info("SSE connection timed out")
            isDisconnected = true
            callback()
        }
    }

    // Add method to check if client is still connected
    fun isConnected(): Boolean = !isDisconnected

    companion object {
        private val LOG = LoggerFactory.getLogger(SseEmitterBasedEventEmitter::class.java)
    }
}
