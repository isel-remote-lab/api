package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.utils.Either

typealias CreateHardwareResult = Either<ServicesExceptions, Hardware>
typealias GetHardwareByIdResult = Either<ServicesExceptions, Hardware>

interface IHardwareService {
    fun createHardware(
        name: String,
        serialNumber: String,
        status: String? = null,
        macAddress: String? = null,
        ipAddress: String? = null,
    ): CreateHardwareResult

    fun getHardwareById(id: String): GetHardwareByIdResult
}