package isel.rl.core.repository

import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.domain.laboratory.session.LabSessionState
import kotlinx.datetime.Instant

interface LabSessionRepository {
    fun createLabSession(
        labId: Int,
        hwId: Int,
        ownerId: Int,
        startTime: Instant,
        endTime: Instant,
        state: LabSessionState,
    ): Int

    fun getLabSessionById(labSessionId: Int): LabSession?

    fun getLabSessionsByLabId(labId: Int): List<LabSession>

    fun getLabSessionsByUserId(userId: Int): List<LabSession>

    fun isUserInSession(userId: Int): Boolean

    fun updateLabSession(
        labSessionId: Int,
        startTime: Instant? = null,
        endTime: Instant? = null,
        state: LabSessionState?,
    ): Boolean

    fun removeLabSessionById(labSessionId: Int): Boolean
}
