package isel.rl.core.domain.hardware.domain

import isel.rl.core.domain.config.HardwareDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.*
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import java.util.*

@Component
data class HardwareDomain(
    private val domainConfig: HardwareDomainConfig,
) {
    fun validateCreateHardware(
        name: String?,
        serialNumber: String?,
        status: String?,
        macAddress: String?,
        ipAddress: String?,
        createdAt: Instant
    ): Hardware {
        val validatedName = when {
            domainConfig.isHardwareNameOptional && name.isNullOrBlank() -> HardwareName()
            name.isNullOrBlank() ->
                throw ServicesExceptions.Hardware.InvalidHardwareName(
                    "Hardware name must be between ${domainConfig.minHardwareNameLength} and " +
                            "${domainConfig.maxHardwareNameLength} characters",
                )

            else -> validateHardwareName(name)
        }

        return Hardware(
            name = validatedName,
            serialNumber = SerialNumber(serialNumber!!),
            status = when(status?.uppercase(Locale.getDefault())) {
                "A" -> HardwareStatus.Available
                "O" -> HardwareStatus.Occupied
                "M" -> HardwareStatus.Maintenance
                else -> throw ServicesExceptions.Hardware.InvalidHardwareStatus(
                    "Hardware status must be one of: A (Available), O (Occupied), M (Maintenance)"
                )
            },
            macAddress = if (macAddress == null) null else MacAddress(macAddress),
            ipAddress = if (ipAddress == null) null else IpAddress(ipAddress),
            createdAt = createdAt
        )
    }

    fun validateHardwareId(id: String) =
        runCatching { id.toInt() }.getOrElse { throw ServicesExceptions.Hardware.InvalidHardwareId }

    fun validateHardwareName(name: String): HardwareName {
        if (name.length !in domainConfig.minHardwareNameLength..domainConfig.maxHardwareNameLength) {
            throw ServicesExceptions.Hardware.InvalidHardwareName(
                "Hardware name must be between ${domainConfig.minHardwareNameLength} and " +
                        "${domainConfig.maxHardwareNameLength} characters",
            )
        }
        return HardwareName(name)
    }

    /*
    fun validateHardwareSerialNumber(serialNumber: String): Boolean {
        return serialNumber.length in domainConfig.hardwareSerialNumberMinLength..domainConfig.hardwareSerialNumberMaxLength
    }

    fun validateHardwareMacAddress(macAddress: String?): Boolean {
        return macAddress == null || macAddress.length in domainConfig.hardwareMacAddressMinLength..domainConfig.hardwareMacAddressMaxLength
    }

    fun validateHardwareIpAddress(ipAddress: String?): Boolean {
        return ipAddress == null || ipAddress.length in domainConfig.hardwareIpAddressMinLength..domainConfig.hardwareIpAddressMaxLength
    }

    fun validateHardwareStatus(status: String?): Boolean {
        return status == null || status.length in domainConfig.hardwareStatusMinLength..domainConfig.hardwareStatusMaxLength
    }

     */
}
