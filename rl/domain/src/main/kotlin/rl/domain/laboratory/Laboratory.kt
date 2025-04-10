package rl.domain.laboratory

import kotlinx.datetime.Instant

data class Laboratory(
    val id: Int,
    val labName: LabName,
    val labDescription: LabDescription,
    val labDuration: Int,
    val labQueueLimit: Int,
    val createdAt: Instant,
    val ownerId: Int
)
