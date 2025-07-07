package isel.rl.core.services

import EventEmitter
import isel.rl.core.domain.events.Event
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.HardwareStatus
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.domain.laboratory.session.LabSessionState
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.ILabSessionService
import isel.rl.core.services.interfaces.ILabWaitingQueueService
import isel.rl.core.services.interfaces.ValidateLabSessionCreationResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
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
    private val labWaitingQueueService: ILabWaitingQueueService,
) : ILabSessionService {
    override fun handleLabSessionCreation(
        labId: String,
        userId: Int,
        listener: EventEmitter,
    ): ValidateLabSessionCreationResult =
        runCatching {
            LOG.info("Validating lab session creation for labId: $labId, userId: $userId")
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)

            transactionManager.run {
                // Check if a user is in a session
                if (it.labSessionRepository.isUserInSession(userId)) {
                    LOG.info("User $userId is already in a session")
                    return@run failure(ServicesExceptions.LabSessions.UserAlreadyInSession)
                }

                if (it.laboratoriesRepository.checkIfUserBelongsToLaboratory(labId = validatedLabId, userId = userId)) {
                    return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                }

                val laboratory =
                    it.laboratoriesRepository.getLaboratoryById(validatedLabId)
                        ?: return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                // Handle queue logic and hardware availability
                when (it.labWaitingQueueRepository.isLabQueueEmpty(validatedLabId)) {
                    true -> {
                        LOG.info("Lab Queue is empty")
                        handleEmptyQueue(laboratory, userId, listener)
                    }

                    false -> {
                        LOG.info("Lab Queue is not empty")
                        handleFullLabQueue(laboratory, userId, listener)
                    }
                }
                success(Unit)
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    private fun handleEmptyQueue(
        laboratory: Laboratory,
        userId: Int,
        listener: EventEmitter,
    ): ValidateLabSessionCreationResult {
        // If there is no hardware available
        val availableHardware = getAvailableHardware(laboratory.id)
        return if (availableHardware.isEmpty()) {
            LOG.info("No available hardware for labId: {}", laboratory.id)

            handleNoAvailableHardware(laboratory, userId, listener)

            success(Unit)
        } else {
            LOG.info("Available hardware for labId: {} found", laboratory.id)

            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                startLabSession(
                    listener = listener,
                    labSession =
                        createLabSession(
                            labId = laboratory.id,
                            hwId = availableHardware.random(),
                            ownerId = userId,
                            labDuration = laboratory.duration.labDurationInfo!!,
                            state = LabSessionState.InProgress,
                        ),
                )
            }

            success(Unit)
        }
    }

    private fun handleFullLabQueue(
        laboratory: Laboratory,
        userId: Int,
        listener: EventEmitter,
    ): ValidateLabSessionCreationResult {
        val availableHardware = getAvailableHardware(laboratory.id)
        return if (availableHardware.isEmpty()) {
            LOG.info("No available hardware for labId: {}", laboratory.id)

            handleNoAvailableHardware(laboratory, userId, listener)

            success(Unit)
        } else {
            // TODO: Review this else. This only happens if:
            // 1 - a hardware is created and associated to the lab without sending a notification
            // 2 - a problem occurs with a ongoing session and the hardware is made available but no notification is sent.
            availableHardware.forEach { hwId ->
                LOG.info("Available hardware for labId: {} found with id {}", laboratory.id, hwId)

                labWaitingQueueService.popUserFromQueue(laboratory.id)
            }

            // TODO: Insert current user in queue if there is still a available hardware and do the other tasks
            success(Unit)
        }
    }

    private fun handleNoAvailableHardware(
        laboratory: Laboratory,
        userId: Int,
        listener: EventEmitter,
    ) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            // Await response (suspension point)
            labWaitingQueueService.pushUserIntoQueue(labId = laboratory.id, userId = userId, listener = listener)

            startLabSession(
                listener = listener,
                labSession =
                    createLabSession(
                        labId = laboratory.id,
                        hwId = getAvailableHardware(laboratory.id).random(),
                        ownerId = userId,
                        labDuration = laboratory.duration.labDurationInfo!!,
                        state = LabSessionState.InProgress,
                    ),
            )
        }
    }

    private fun createLabSession(
        labId: Int,
        hwId: Int,
        ownerId: Int,
        labDuration: Duration,
        state: LabSessionState,
    ): LabSession {
        LOG.info("Available hardware found for labId: $labId")

        val startTime = clock.now()
        val endtime =
            startTime.plus(labDuration).toJavaInstant().toKotlinInstant()

        return transactionManager.run {
            val sessionId =
                it.labSessionRepository.createLabSession(
                    labId = labId,
                    hwId = hwId,
                    ownerId = ownerId,
                    startTime = startTime,
                    endTime = endtime,
                    state = state,
                )

            it.hardwareRepository.updateHardware(
                hwId = hwId,
                hwStatus = HardwareStatus.Occupied,
            )

            LabSession(
                id = sessionId,
                labId = labId,
                hwId = hwId,
                ownerId = ownerId,
                startTime = startTime,
                endTime = endtime,
                state = LabSessionState.InProgress,
            )
        }
    }

    private fun getAvailableHardware(labId: Int): List<Int> =
        transactionManager.run {
            it.laboratoriesRepository.getLaboratoryHardware(labId).filter { hwId ->
                it.hardwareRepository.checkHardwareStatus(hwId, HardwareStatus.Available)
            }
        }

    suspend fun startLabSession(
        listener: EventEmitter,
        labSession: LabSession,
    ) {
        lateinit var laboratory: Laboratory
        lateinit var hardware: Hardware

        transactionManager.run {
            laboratory = it.laboratoriesRepository.getLaboratoryById(labSession.labId)
                ?: return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

            hardware = it.hardwareRepository.getHardwareById(labSession.hwId)
                ?: return@run failure(ServicesExceptions.Hardware.HardwareNotFound)
        }

        // TODO: Since this are nullable, maybe emit a error message in the listener and end the connection if null.
        val labDuration =
            laboratory.duration.labDurationInfo!!

        val hardwareIpAddress =
            hardware.ipAddress!!

        // Send initial lab session info
        listener.emit(
            Event.LabSessionStarting(
                eventId = System.currentTimeMillis(),
                labId = laboratory.id.toString(),
                hwId = hardware.id.toString(),
                hwIpAddress = hardwareIpAddress.address,
                labDuration = labDuration.inWholeMinutes.toString(),
                notifyInterval = notifyInterval.inWholeMinutes.toString(),
            ),
        )

        val sessionEndMark = timeSource.markNow() + labDuration

        val shouldStop = AtomicBoolean(false)

        setupListenerCallbacks(listener, labSession.id, hardware.id, shouldStop)

        // Send initial message
        listener.emit(
            Event.LabSessionState(
                eventId = System.currentTimeMillis(),
                type = "session_warning",
                remainingTime = labDuration.inWholeMinutes.toInt(),
                timeUnit = TimeUnit.MINUTES.name,
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
                finishSession(listener, labSession.id, hardware.id)

                if (!transactionManager.run { return@run it.labWaitingQueueRepository.isLabQueueEmpty(laboratory.id) }) {
                    labWaitingQueueService.popUserFromQueue(laboratory.id)
                    labWaitingQueueService.updateQueuePositions(laboratory.id)
                }
            }
        } catch (e: Exception) {
            LOG.error("Error in lab session", e)
            handleSessionError(labSession.id, hardware.id, shouldStop)
        }
    }

    private fun setupListenerCallbacks(
        listener: EventEmitter,
        labSessionId: Int,
        hardwareId: Int,
        shouldStop: AtomicBoolean,
    ) {
        listener.onCompletion {
            LOG.info("SSE connection completed")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
            updateHardwareStatusToAvailable(hardwareId)
        }

        listener.onError { throwable ->
            LOG.warn("SSE connection error: ${throwable.message}")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
            updateHardwareStatusToAvailable(hardwareId)
        }

        listener.onTimeout {
            LOG.info("SSE connection timed out")
            shouldStop.set(true)
            updateLabSessionToCompleted(labSessionId)
            updateHardwareStatusToAvailable(hardwareId)
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
                Event.LabSessionState(
                    eventId = generateEventId(),
                    type = "session_warning",
                    remainingTime = remainingDuration.inWholeMinutes.toInt(),
                    timeUnit = TimeUnit.MINUTES.name,
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
                Event.LabSessionState(
                    eventId = generateEventId(),
                    type = "session_ending",
                    remainingTime = remainingDuration.inWholeSeconds.toInt(),
                    timeUnit = TimeUnit.SECONDS.name,
                ),
            )
        }
    }

    private fun finishSession(
        listener: EventEmitter,
        labSessionId: Int,
        hardwareId: Int,
    ) {
        LOG.info("Lab session time completed")
        listener.emit(
            Event.LabSessionState(
                eventId = generateEventId(),
                type = "session_finished",
            ),
        )
        listener.complete()
        updateLabSessionToCompleted(labSessionId)
        updateHardwareStatusToAvailable(hardwareId)
    }

    private fun handleSessionError(
        labSessionId: Int,
        hardwareId: Int,
        shouldStop: AtomicBoolean,
    ) {
        shouldStop.set(true)
        updateLabSessionToCompleted(labSessionId)
        updateHardwareStatusToAvailable(hardwareId)
    }

    private fun updateLabSessionToCompleted(labSessionId: Int) =
        runCatching {
            transactionManager.run {
                it.labSessionRepository.updateLabSession(
                    labSessionId = labSessionId,
                    endTime = clock.now(),
                    state = LabSessionState.Completed,
                )
            }
        }.getOrElse { e ->
            LOG.error("Error updating lab session to completed")
        }

    private fun updateHardwareStatusToAvailable(hardwareId: Int) =
        runCatching {
            transactionManager.run {
                it.hardwareRepository.updateHardware(
                    hwId = hardwareId,
                    hwStatus = HardwareStatus.Available,
                )
            }
        }.getOrElse { e ->
            LOG.error("Error updating hardware status to available", e)
        }

    private fun generateEventId(): Long = System.currentTimeMillis()

    companion object {
        private val LOG = LoggerFactory.getLogger(LabSessionService::class.java)

        val timeSource = TimeSource.Monotonic
        private val notifyInterval = 1.minutes
        private val countDownMarkValue = 15.seconds
    }
}
