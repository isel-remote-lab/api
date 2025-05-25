package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.group.GroupCreateInputModel
import isel.rl.core.http.model.group.GroupOutputModel
import isel.rl.core.http.model.group.GroupWithUsersOutputModel
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IGroupsService
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
class GroupsController(
    private val groupsService: IGroupsService,
) {
    @PostMapping(Uris.Groups.CREATE)
    fun createGroup(
        user: AuthenticatedUser,
        @RequestBody input: GroupCreateInputModel,
    ): ResponseEntity<*> =
        when (val result = groupsService.createGroup(input.groupName, input.groupDescription, user.user)) {
            is Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse(
                        message = "Group created successfully",
                        data = GroupOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Groups.GET_BY_ID)
    fun getById(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = groupsService.getGroupById(id)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "Group retrieved successfully",
                        data = GroupWithUsersOutputModel.mapOf(result.value),
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Groups.GET_USER_GROUPS)
    fun getUserGroups(
        user: AuthenticatedUser,
        @RequestParam userId: String?,
        @RequestParam limit: String?,
        @RequestParam skip: String?,
    ): ResponseEntity<*> =
        when (val result = groupsService.getUserGroups(userId ?: user.user.id.toString(), limit, skip)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "Groups retrieved successfully",
                        data = result.value.map { GroupOutputModel.mapOf(it) },
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @PatchMapping(Uris.Groups.ADD_USER_TO_GROUP)
    fun addUserToGroup(
        user: AuthenticatedUser,
        // Group Id
        @PathVariable id: String,
        @RequestParam userId: String?,
    ): ResponseEntity<*> =
        when (val result = groupsService.addUserToGroup(user.user.id, userId, id)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "User added to group successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @DeleteMapping(Uris.Groups.REMOVE_USER_FROM_GROUP)
    fun removeUserFromGroup(
        user: AuthenticatedUser,
        // Group Id
        @PathVariable id: String,
        @RequestParam userId: String?,
    ): ResponseEntity<*> =
        when (val result = groupsService.removeUserFromGroup(user.user.id, userId, id)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "User removed from group successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }

    @DeleteMapping(Uris.Groups.DELETE)
    fun deleteGroup(
        user: AuthenticatedUser,
        // Group Id
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = groupsService.deleteGroup(user.user.id, id)) {
            is Success ->
                ResponseEntity.ok(
                    SuccessResponse(
                        message = "Group deleted successfully",
                    ),
                )

            is Failure -> handleServicesExceptions(result.value)
        }
}
