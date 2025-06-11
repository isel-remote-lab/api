package isel.rl.core.domain.hardware

import kotlinx.datetime.Instant

data class Hardware(
    val id: Int,
    val name: HardwareName,
    val serialNum: String,
    val status: HardwareStatus,
    val macAddress: String?,
    val ipAddress: String?,
    val createdAt: Instant,
)
