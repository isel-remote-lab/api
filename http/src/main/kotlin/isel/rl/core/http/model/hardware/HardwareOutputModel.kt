package isel.rl.core.http.model.hardware

import isel.rl.core.domain.hardware.Hardware

data class HardwareOutputModel(
    val id: String,
    val name: String,
    val serialNumber: String,
    val status: String? = null,
    val macAddress: String? = null,
    val ipAddress: String? = null
) {
    companion object {
        fun mapOf(hardware: Hardware): HardwareOutputModel =
            HardwareOutputModel(
                id = hardware.id.toString(),
                name = hardware.name.hardwareNameInfo,
                serialNumber = hardware.serialNumber.serialNumberInfo,
                status = hardware.status.char,
                macAddress = hardware.macAddress?.address,
                ipAddress = hardware.ipAddress?.address
            )
    }
}

