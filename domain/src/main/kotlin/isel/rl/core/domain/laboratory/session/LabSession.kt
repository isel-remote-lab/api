package isel.rl.core.domain.laboratory.session

import kotlinx.datetime.Instant

data class LabSession(
    val id: Int = 0,
    val labId: Int,
    val ownerId: Int,
    val startTime: Instant,
    val endTime: Instant,
    val state: LabSessionState,
)
