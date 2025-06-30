package isel.rl.core.services

import EventEmitter
import isel.rl.core.domain.events.Event
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.domain.laboratory.session.LabSessionState
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateLabSessionResult
import isel.rl.core.services.interfaces.ILabSessionService
import isel.rl.core.services.interfaces.StartLabSessionResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Service
data class LabSessionService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val laboratoriesDomain: LaboratoriesDomain,
) : ILabSessionService {
    override fun createLabSession(
        labId: String,
        userId: Int,
    ): CreateLabSessionResult =
        runCatching {
            LOG.info("Creating lab session for labId: $labId, userId: $userId")
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val startTime = clock.now()

            transactionManager.run {
                val repo = it.labSessionRepository

                val laboratory =
                    it.laboratoriesRepository.getLaboratoryById(validatedLabId)
                        ?: return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                val endtime = startTime.plus(laboratory.duration.labDurationInfo!!).toJavaInstant().toKotlinInstant()

                val sessionId =
                    repo.createLabSession(
                        labId = validatedLabId,
                        ownerId = userId,
                        startTime = startTime,
                        endTime = endtime,
                        state = LabSessionState.InProgress,
                    )

                success(
                    LabSession(
                        id = sessionId,
                        labId = validatedLabId,
                        ownerId = userId,
                        startTime = startTime,
                        endTime = endtime,
                        state = LabSessionState.InProgress,
                    ),
                )
            }
        }.getOrElse { e ->
            LOG.error("Error creating lab session for labId: $labId, userId: $userId", e)
            handleException(e as Exception)
        }

    override suspend fun startLabSession(
        listener: EventEmitter,
        labId: Int,
        labSessionId: Int,
    ): StartLabSessionResult {
        lateinit var laboratory: Laboratory

        transactionManager.run {
            laboratory = it.laboratoriesRepository.getLaboratoryById(labId)
                ?: return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
        }

        val labDuration =
            laboratory.duration.labDurationInfo ?: return failure(ServicesExceptions.UnexpectedError)

        val sessionEndMark = timeSource.markNow() + labDuration

        val shouldStop = AtomicBoolean(false)

        setupListenerCallbacks(listener, labSessionId, shouldStop)

        // Send initial message
        listener.emit(
            Event.Message(
                eventId = System.currentTimeMillis(),
                type = "session_warning",
                remainingTime = labDuration.inWholeMinutes.toInt(),
            ),
        )

        try {
            val remainingDuration =
                runWarningPhase(
                    listener = listener,
                    shouldStop = shouldStop,
                    countDownStartMark = sessionEndMark,
                    initialDuration = labDuration,
                )

            runCountdownPhase(
                listener = listener,
                shouldStop = shouldStop,
                countDownStartMark = sessionEndMark,
                remainingDuration = remainingDuration,
            )

            if (!shouldStop.get()) {
                finishSession(listener, labSessionId)
            }
        } catch (e: CancellationException) {
            LOG.info("Lab session cancelled")
            throw e // Re-throw cancellation
        } catch (e: Exception) {
            LOG.error("Error in lab session", e)
            handleSessionError(labSessionId, shouldStop)
        }

        return success(Unit)
    }

    private fun setupListenerCallbacks(
        listener: EventEmitter,
        labSessionId: Int,
        shouldStop: AtomicBoolean,
    ) {
        listener.onCompletion {
            LOG.info("SSE connection completed")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
        }

        listener.onError { throwable ->
            LOG.warn("SSE connection error: ${throwable.message}")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
        }

        listener.onTimeout {
            LOG.info("SSE connection timed out")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
        }
    }

    private suspend fun runWarningPhase(
        listener: EventEmitter,
        shouldStop: AtomicBoolean,
        countDownStartMark: TimeMark,
        initialDuration: Duration,
    ): Duration {
        var remainingDuration = initialDuration

        while (countDownStartMark.hasNotPassedNow() && !shouldStop.get()) {
            // Check if remaining time is smaller than the notify interval
            // If true, calculate a delayTime to allow the countdown timer to be executed
            if (remainingDuration <= notifyInterval) {
                val delayTime = remainingDuration - countDownMarkValue
                delay(delayTime)
                remainingDuration = remainingDuration - delayTime
                break
            }

            delay(notifyInterval)

            if (shouldStop.get()) {
                LOG.info("Stopping lab session due to client disconnection")
                break
            }

            remainingDuration = remainingDuration.minus(notifyInterval)
            LOG.debug("Emitting session warning message, remaining: ${remainingDuration.inWholeMinutes}min")

            listener.emit(
                Event.Message(
                    eventId = generateEventId(),
                    type = "session_warning",
                    remainingTime = remainingDuration.inWholeMinutes.toInt(),
                ),
            )
        }
        return remainingDuration
    }

    private suspend fun runCountdownPhase(
        listener: EventEmitter,
        shouldStop: AtomicBoolean,
        countDownStartMark: TimeMark,
        remainingDuration: Duration,
    ) {
        var remainingDuration = remainingDuration
        while (countDownStartMark.hasNotPassedNow() && !shouldStop.get()) {
            delay(1.seconds)
            remainingDuration = remainingDuration.minus(1.seconds)

            listener.emit(
                Event.Message(
                    eventId = generateEventId(),
                    type = "session_ending",
                    remainingTime = remainingDuration.inWholeSeconds.toInt(),
                ),
            )
        }
    }

    private suspend fun finishSession(
        listener: EventEmitter,
        labSessionId: Int,
    ) {
        LOG.info("Lab session time completed")
        listener.emit(
            Event.Message(
                eventId = generateEventId(),
                type = "session_finished",
            ),
        )
        listener.complete()
        updateLabSessionToCompleted(labSessionId)
    }

    private fun handleSessionError(
        labSessionId: Int,
        shouldStop: AtomicBoolean,
    ) {
        shouldStop.set(true)
        updateLabSessionToCompleted(labSessionId)
    }

    private fun updateLabSessionToCompleted(labSessionId: Int) {
        try {
            transactionManager.run {
                it.labSessionRepository.updateLabSession(
                    labSessionId = labSessionId,
                    endTime = clock.now(),
                    state = LabSessionState.Completed,
                )
            }
        } catch (e: Exception) {
            LOG.error("Error updating lab session to completed", e)
        }
    }

    private fun generateEventId(): Long = System.currentTimeMillis()

    companion object {
        private val LOG = LoggerFactory.getLogger(LabSessionService::class.java)

        val timeSource = TimeSource.Monotonic
        private val notifyInterval = 1.minutes
        private val countDownMarkValue = 15.seconds
    }
}
