package rl.domain.hardware

import kotlinx.datetime.Instant

data class Hardware(
    val id: Int,
    val name: String,
    val serialNumber: String,
    val status: HardwareStatus,
    val macAddress: String,
    val ipAddress: String,
    val createdAt: Instant
)
