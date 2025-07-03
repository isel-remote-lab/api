package isel.rl.core.domain.events

import kotlinx.datetime.Instant
import org.springframework.http.HttpStatusCode

/**
 * Represents an event that can be sent to the client.
 */
sealed interface Event {
    data class LabSessionState(
        val eventId: Long,
        val type: String,
        val remainingTime: Int? = null,
        val timeUnit: String? = null,
    ) : Event

    data class LabSessionStarting(
        val eventId: Long,
        val labId: String,
        val hwId: String,
        val hwIpAddress: String,
        val labDuration: String,
        val notifyInterval: String,
    ) : Event

    data class WaitingQueue(
        val eventId: Long,
        val labId: String,
        val waitingQueuePos: Int,
    ) : Event

    data class Message(
        val eventId: Long,
        val message: String,
    ) : Event

    /**
     * Represents a error event.
     *
     * @param statusCode the status code of the error
     * @param message the message of the error
     */
    data class Error(val statusCode: HttpStatusCode, val message: String) : Event

    /**
     * Represents a keep-alive event.
     *
     * @param timestamp the timestamp of the keep-alive
     */
    data class KeepAlive(val timestamp: Instant) : Event
}
