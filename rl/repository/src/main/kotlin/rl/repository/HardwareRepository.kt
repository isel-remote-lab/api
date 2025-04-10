package rl.repository

import kotlinx.datetime.Instant
import rl.domain.hardware.Hardware
import rl.domain.hardware.HardwareName
import rl.domain.hardware.HardwareStatus

interface HardwareRepository {
    fun createHardware(
        name: HardwareName,
        serialNum: String,
        status: HardwareStatus,
        macAddress: String?,
        ipAddress: String?,
        createdAt: Instant
    ): Int

    fun getHardwareById(hwId: Int): Hardware?

    fun getHardwareByName(hwName: HardwareName): List<Hardware>

    fun updateHardwareName(hwId: Int, hwName: HardwareName): Boolean

    fun updateHardwareStatus(hwId: Int, hwStatus: HardwareStatus): Boolean

    fun updateHardwareIpAddress(hwId: Int, ipAddress: String): Boolean

    fun updateHardwareMacAddress(hwId: Int, macAddress: String): Boolean

    fun deleteHardware(hwId: Int): Boolean
}