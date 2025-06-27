package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.props.Role
import isel.rl.core.http.annotations.RequireRole
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.hardware.HardwareCreateInputModel
import isel.rl.core.http.model.hardware.HardwareOutputModel
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IHardwareService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
data class HardwareController(
    private val hardwareService: IHardwareService,
) {
    @RequireRole(Role.TEACHER)
    @PostMapping(Uris.Hardware.CREATE)
    fun createHardware(
        user: AuthenticatedUser,
        @RequestBody input: HardwareCreateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                hardwareService.createHardware(
                    name = input.name,
                    serialNumber = input.serialNumber,
                    status = input.status,
                    macAddress = input.macAddress,
                    ipAddress = input.ipAddress,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse(
                        message = "Hardware created successfully",
                        data = HardwareOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
    @GetMapping(Uris.Hardware.GET_BY_ID)
    fun getHardwareById(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = hardwareService.getHardwareById(id)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "Hardware retrieved successfully",
                        data = HardwareOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
    @GetMapping(Uris.Hardware.GET_ALL_HARDWARE)
    fun getAllHardware(
        user: AuthenticatedUser,
        @RequestParam status: String? = null,
        @RequestParam limit: String? = null,
        @RequestParam skip: String? = null,
    ): ResponseEntity<*> =
        when (
            val result =
                hardwareService.getAllHardware(
                    limit = limit,
                    skip = skip,
                    status = status,
                )
        ) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "Hardware retrieved successfully",
                        data = result.value.map { hardware -> HardwareOutputModel.mapOf(hardware) },
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }
}
