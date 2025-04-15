package isel.rl.core.repository

import isel.rl.core.domain.laboratory.LabSession
import isel.rl.core.domain.laboratory.LabSessionState
import kotlinx.datetime.Instant

interface LabSessionRepository {
    fun createLabSession(
        labId: Int,
        ownerId: Int,
        startTime: Instant,
        endTime: Instant,
        state: LabSessionState,
    ): Int

    fun getLabSessionById(labSessionId: Int): LabSession?

    fun getLabSessionsByLabId(labId: Int): List<LabSession>

    fun getLabSessionsByUserId(userId: Int): List<LabSession>

    fun updateLabSession(
        labSessionId: Int,
        startTime: Instant?,
        endTime: Instant?,
        state: LabSessionState?,
    ): Boolean

    fun removeLabSessionById(labSessionId: Int): Boolean
}
