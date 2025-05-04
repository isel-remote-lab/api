package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.laboratory.LaboratoryCreateInputModel
import isel.rl.core.http.model.laboratory.LaboratoryOutputModel
import isel.rl.core.http.model.laboratory.LaboratoryUpdateInputModel
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
data class LaboratoriesController(
    private val laboratoriesService: ILaboratoriesService,
) {
    @PostMapping(Uris.Laboratories.CREATE)
    fun createLaboratory(
        user: AuthenticatedUser,
        @RequestBody input: LaboratoryCreateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.createLaboratory(
                    labName = input.labName,
                    labDescription = input.labDescription,
                    labDuration = input.labDuration,
                    labQueueLimit = input.labQueueLimit,
                    ownerId = user.user.id,
                )
        ) {
            is Success -> {
                ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse(
                        message = "Laboratory created successfully",
                        data =
                            mapOf(
                                "laboratory_id" to result.value,
                            ),
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Laboratories.GET)
    fun getLaboratoryById(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = laboratoriesService.getLaboratoryById(id, user.user.id)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory found with the id $id",
                        data = LaboratoryOutputModel.mapOf(result.value),
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @PatchMapping(Uris.Laboratories.UPDATE)
    fun updateLaboratory(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestBody input: LaboratoryUpdateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.updateLaboratory(
                    labId = id,
                    labName = input.labName,
                    labDescription = input.labDescription,
                    labDuration = input.labDuration,
                    labQueueLimit = input.labQueueLimit,
                    ownerId = user.user.id,
                )
        ) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory updated successfully",
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Laboratories.GET_ALL_BY_USER)
    fun getUserLaboratories(
        user: AuthenticatedUser,
        @RequestParam limit: String?,
        @RequestParam skip: String?,
    ): ResponseEntity<*> =
        when (val result = laboratoriesService.getAllLaboratoriesByUser(user.user.id, limit, skip)) {
            is Success -> {
                val laboratories = result.value

                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratories found for user with id ${user.user.id}",
                        data = laboratories.map { laboratory ->
                            LaboratoryOutputModel.mapOf(laboratory)
                        },
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }
}
