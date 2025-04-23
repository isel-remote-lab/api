package isel.rl.core.repository

import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import kotlinx.datetime.Instant

interface HardwareRepository {
    fun createHardware(
        name: HardwareName,
        serialNum: String,
        status: HardwareStatus,
        macAddress: String?,
        ipAddress: String?,
        createdAt: Instant,
    ): Int

    fun getHardwareById(hwId: Int): Hardware?

    fun getHardwareByName(hwName: HardwareName): List<Hardware>

    fun updateHardware(
        hwId: Int,
        hwName: HardwareName? = null,
        hwStatus: HardwareStatus? = null,
        ipAddress: String? = null,
        macAddress: String? = null,
    ): Boolean

    fun deleteHardware(hwId: Int): Boolean
}
