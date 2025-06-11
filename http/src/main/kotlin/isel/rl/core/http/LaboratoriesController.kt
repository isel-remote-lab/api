package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.props.Role
import isel.rl.core.http.annotations.RequireRole
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.group.GroupOutputModel
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
import org.springframework.web.bind.annotation.DeleteMapping
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
    @RequireRole(Role.TEACHER)
    @PostMapping(Uris.Laboratories.CREATE)
    fun createLaboratory(
        user: AuthenticatedUser,
        @RequestBody input: LaboratoryCreateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.createLaboratory(
                    name = input.name,
                    description = input.description,
                    duration = input.duration,
                    queueLimit = input.queueLimit,
                    owner = user.user,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse(
                        message = "Laboratory created successfully",
                        data = LaboratoryOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Laboratories.GET_BY_ID)
    fun getLaboratoryById(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = laboratoriesService.getLaboratoryById(id, user.user.id)) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory found with the id $id",
                        data = LaboratoryOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
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
                    labName = input.name,
                    labDescription = input.description,
                    labDuration = input.duration,
                    labQueueLimit = input.queueLimit,
                    ownerId = user.user.id,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory updated successfully",
                    ),
                )

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
                        message = "Laboratories retrieved successfully",
                        data =
                            laboratories.map { laboratory ->
                                LaboratoryOutputModel.mapOf(laboratory)
                            },
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Laboratories.GET_LABORATORY_GROUPS)
    fun getLaboratoryGroups(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestParam limit: String? = null,
        @RequestParam skip: String? = null,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.getLaboratoryGroups(
                    labId = id,
                    limit = limit,
                    skip = skip,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory groups retrieved successfully",
                        data =
                            result.value.map { group ->
                                GroupOutputModel.mapOf(group)
                            },
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
    @PatchMapping(Uris.Laboratories.ADD_GROUP_TO_LABORATORY)
    fun addGroupToLaboratory(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestParam groupId: String? = null,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.addGroupToLaboratory(
                    labId = id,
                    groupId = groupId,
                    ownerId = user.user.id,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Group added to laboratory successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
    @DeleteMapping(Uris.Laboratories.REMOVE_GROUP_FROM_LABORATORY)
    fun removeGroupFromLaboratory(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestParam groupId: String? = null,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.removeGroupFromLaboratory(
                    labId = id,
                    groupId = groupId,
                    ownerId = user.user.id,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Group removed from laboratory successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @RequireRole(Role.TEACHER)
    @DeleteMapping(Uris.Laboratories.DELETE)
    fun deleteLaboratory(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (
            val result =
                laboratoriesService.deleteLaboratory(
                    labId = id,
                    ownerId = user.user.id,
                )
        ) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "Laboratory deleted successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }
}
