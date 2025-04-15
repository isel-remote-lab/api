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

    fun updateHardwareName(
        hwId: Int,
        hwName: HardwareName,
    ): Boolean

    fun updateHardwareStatus(
        hwId: Int,
        hwStatus: HardwareStatus,
    ): Boolean

    fun updateHardwareIpAddress(
        hwId: Int,
        ipAddress: String,
    ): Boolean

    fun updateHardwareMacAddress(
        hwId: Int,
        macAddress: String,
    ): Boolean

    fun deleteHardware(hwId: Int): Boolean
}
