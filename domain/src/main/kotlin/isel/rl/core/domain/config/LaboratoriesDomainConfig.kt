package isel.rl.core.domain.config

import kotlin.time.Duration

data class LaboratoriesDomainConfig(
    val minLengthLabName: Int,
    val maxLengthLabName: Int,
    val minLengthLabDescription: Int,
    val maxLengthLabDescription: Int,
    val minLabDuration: Duration,
    val maxLabDuration: Duration,
    val minLabQueueLimit: Int,
    val maxLabQueueLimit: Int,
)
