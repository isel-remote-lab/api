package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.annotations.RequireApiKey
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.hardware.HardwareCreateInputModel
import isel.rl.core.http.model.hardware.HardwareOutputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IHardwareService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
data class HardwareController(
    private val hardwareService: IHardwareService
) {
    @PostMapping(Uris.Hardware.CREATE)
    @RequireApiKey
    fun createHardware(
        @RequestBody input: HardwareCreateInputModel
    ): ResponseEntity<*> =
        when (val result = hardwareService.createHardware(
            name = input.name,
            serialNumber = input.serialNumber,
            status = input.status,
            macAddress = input.macAddress,
            ipAddress = input.ipAddress
        )) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse(
                    message = "Hardware created successfully",
                    data = HardwareOutputModel.mapOf(result.value)
                )
            )
            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Hardware.GET_BY_ID)
    @RequireApiKey
    fun getHardwareById(
        @PathVariable id: String
    ): ResponseEntity<*> =
        when (val result = hardwareService.getHardwareById(id)) {
            is Success -> ResponseEntity.ok(
                SuccessResponse(
                    message = "Hardware retrieved successfully",
                    data = HardwareOutputModel.mapOf(result.value)
                )
            )
            is Failure -> handleServicesExceptions(result.value)
        }
}