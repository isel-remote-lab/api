package rl.domain.laboratory

import kotlinx.datetime.Instant

data class LabSession(
    val id: Int,
    val labId: Int,
    val ownerId: Int,
    val startTime: Instant,
    val endTime: Instant,
    val state: LabSessionState,
)
