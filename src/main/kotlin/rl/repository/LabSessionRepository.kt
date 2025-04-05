package rl.repository

import kotlinx.datetime.Instant
import rl.domain.laboratory.LabSession
import rl.domain.laboratory.LabSessionState

interface LabSessionRepository {
    fun createLabSession(
        labId: Int,
        ownerId: Int,
        startTime: Instant,
        endTime: Instant,
        state: LabSessionState
    ): Int

    fun getLabSessionById(labSessionId: Int): LabSession?

    fun getLabSessionsByLabId(labId: Int): List<LabSession>

    fun getLabSessionsByUserId(userId: Int): List<LabSession>

    fun updateLabSession(
        labSessionId: Int,
        startTime: Instant?,
        endTime: Instant?,
        state: LabSessionState?
    ): Boolean

    fun removeLabSessionById(labSessionId: Int): Boolean
}