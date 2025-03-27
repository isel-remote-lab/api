package rl.domain.laboratory

import kotlinx.datetime.Instant
import java.time.Duration

data class Laboratory(
    val id: Int,
    val name: String,
    val duration: Duration,
    val createdAt: Instant,
    val ownerId: Int
)
