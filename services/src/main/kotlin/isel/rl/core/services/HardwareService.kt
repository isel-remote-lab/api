package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.domain.HardwareDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateHardwareResult
import isel.rl.core.services.interfaces.GetHardwareByIdResult
import isel.rl.core.services.interfaces.IHardwareService
import isel.rl.core.services.utils.handleException
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
data class HardwareService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val hardwareDomain: HardwareDomain
) : IHardwareService {
    override fun createHardware(
        name: String,
        serialNumber: String,
        status: String?,
        macAddress: String?,
        ipAddress: String?
    ): CreateHardwareResult = runCatching {
        val validatedHardware = hardwareDomain.validateCreateHardware(
            name = name,
            serialNumber = serialNumber,
            status = status,
            macAddress = macAddress,
            ipAddress = ipAddress,
            createdAt = clock.now()
        )

        transactionManager.run {
            val id = it.hardwareRepository.createHardware(validatedHardware)
            return@run success(validatedHardware.copy(id = id))
        }
    }.getOrElse { e ->
        handleException(e as Exception)
    }

    override fun getHardwareById(id: String): GetHardwareByIdResult =
        runCatching {
            val validatedId = hardwareDomain.validateHardwareId(id)
            transactionManager.run {
                it.hardwareRepository.getHardwareById(validatedId)?.let { hardware ->
                    return@run success(hardware)
                } ?: throw ServicesExceptions.Hardware.HardwareNotFound
            }
        }.getOrElse { e ->
            println(e.message)
            handleException(e as Exception)
        }
}
