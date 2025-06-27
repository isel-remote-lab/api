package isel.rl.core.domain.hardware.domain

import isel.rl.core.domain.config.HardwareDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.HardwareName
import isel.rl.core.domain.hardware.props.HardwareStatus
import isel.rl.core.domain.hardware.props.IpAddress
import isel.rl.core.domain.hardware.props.MacAddress
import isel.rl.core.domain.hardware.props.SerialNumber
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component

@Component
data class HardwareDomain(
    private val domainConfig: HardwareDomainConfig,
) {
    private val hardwareStatusNotInAvailableOptions =
        "Hardware status must be one of the following options: ${
            domainConfig.hardwareStatusOptionsAvailable.forEach { option ->
                option + "\n"
            }
        }"

    private val invalidHardwareNameLength =
        "Hardware name must be between ${domainConfig.minHardwareNameLength} and " +
            "${domainConfig.maxHardwareNameLength} characters"

    fun validateCreateHardware(
        name: String?,
        serialNumber: String?,
        status: String?,
        macAddress: String?,
        ipAddress: String?,
        createdAt: Instant,
    ): Hardware {
        val validatedName =
            when {
                domainConfig.isHardwareNameOptional && name.isNullOrBlank() -> HardwareName()
                name.isNullOrBlank() ->
                    throw ServicesExceptions.Hardware.InvalidHardwareName(
                        invalidHardwareNameLength,
                    )

                else -> validateHardwareName(name)
            }

        val validatedStatus =
            when {
                domainConfig.isHardwareStatusOptional && status.isNullOrBlank() -> null
                status.isNullOrBlank() ->
                    throw ServicesExceptions.Hardware.InvalidHardwareStatus(
                        hardwareStatusNotInAvailableOptions,
                    )

                else -> validateHardwareStatus(status)
            }

        return Hardware(
            name = validatedName,
            serialNumber = SerialNumber(serialNumber!!),
            status = validatedStatus,
            macAddress = if (macAddress == null) null else MacAddress(macAddress),
            ipAddress = if (ipAddress == null) null else IpAddress(ipAddress),
            createdAt = createdAt,
        )
    }

    fun validateHardwareId(id: String) = runCatching { id.toInt() }.getOrElse { throw ServicesExceptions.Hardware.InvalidHardwareId }

    fun validateHardwareName(name: String): HardwareName {
        if (name.length !in domainConfig.minHardwareNameLength..domainConfig.maxHardwareNameLength) {
            throw ServicesExceptions.Hardware.InvalidHardwareName(
                invalidHardwareNameLength,
            )
        }
        return HardwareName(name)
    }

    fun validateHardwareStatus(status: String): HardwareStatus {
        if (status.length > 1 || status !in domainConfig.hardwareStatusOptionsAvailable) {
            throw ServicesExceptions.Hardware.InvalidHardwareStatus(
                hardwareStatusNotInAvailableOptions,
            )
        }

        return HardwareStatus.from(status)
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
