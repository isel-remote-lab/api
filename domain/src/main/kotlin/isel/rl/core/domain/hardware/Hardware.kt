package isel.rl.core.domain.hardware

import isel.rl.core.domain.hardware.props.*
import kotlinx.datetime.Instant

data class Hardware(
    val id: Int = 0,
    val name: HardwareName,
    val serialNumber: SerialNumber,
    val status: HardwareStatus,
    val macAddress: MacAddress? = null,
    val ipAddress: IpAddress? = null,
    val createdAt: Instant,
) {
    companion object {
        const val ID_PROP = "id"
        const val NAME_PROP = "name"
        const val SERIAL_NUMBER_PROP = "serial_number"
        const val STATUS_PROP = "status"
        const val MAC_ADDRESS_PROP = "mac_address"
        const val IP_ADDRESS_PROP = "ip_address"
        const val CREATED_AT_PROP = "created_at"
    }
}
