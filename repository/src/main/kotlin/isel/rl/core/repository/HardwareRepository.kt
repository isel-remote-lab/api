package isel.rl.core.repository

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.HardwareName
import isel.rl.core.domain.hardware.props.HardwareStatus
import isel.rl.core.domain.hardware.props.IpAddress
import isel.rl.core.domain.hardware.props.MacAddress

interface HardwareRepository {
    fun createHardware(validatedCreateHardware: Hardware): Int

    fun getHardwareById(hwId: Int): Hardware?

    fun getHardwareByName(hwName: HardwareName): List<Hardware>

    fun getAllHardware(
        limitAndSkip: LimitAndSkip,
        status: HardwareStatus? = null,
    ): List<Hardware>

    fun checkIfHardwareExists(hardwareId: Int): Boolean

    fun updateHardware(
        hwId: Int,
        hwName: HardwareName? = null,
        hwStatus: HardwareStatus? = null,
        macAddress: MacAddress? = null,
        ipAddress: IpAddress? = null,
    ): Boolean

    fun deleteHardware(hwId: Int): Boolean
}
