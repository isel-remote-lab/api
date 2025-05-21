package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.model.user.UpdateUserRoleInputModel
import isel.rl.core.http.model.user.UserOutputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
data class UsersController(
    private val usersService: IUsersService,
) {
    @GetMapping(Uris.Users.GET)
    fun getById(
        user: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserById(id)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "User found with the id $id",
                        data = UserOutputModel.mapOf(result.value),
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Users.GET_BY_EMAIL)
    fun getByEmail(
        user: AuthenticatedUser,
        @RequestParam email: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserByEmail(email)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "User found with the email $email",
                        data = UserOutputModel.mapOf(result.value),
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @PatchMapping(Uris.Users.UPDATE_USER_ROLE)
    fun updateUserRole(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @RequestBody role: UpdateUserRoleInputModel,
    ): ResponseEntity<*> =
        when (val result = usersService.updateUserRole(user.user, id, role.role)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse(
                        message = "User role updated successfully",
                        data = null,
                    ),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }
}
