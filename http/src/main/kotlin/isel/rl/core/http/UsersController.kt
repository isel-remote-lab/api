package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.User
import isel.rl.core.http.model.user.UserOutputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
data class UsersController(
    private val usersService: IUsersService,
) {
    @GetMapping(Uris.Users.GET)
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserById(id)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    mapOf("user" to result.value.toUserOutput()),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    @GetMapping(Uris.Users.GET_BY_EMAIL)
    fun getByEmail(
        @RequestParam email: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserByEmailOrAuthId(email = email)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    mapOf("user" to result.value.toUserOutput()),
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    private fun User.toUserOutput() =
        UserOutputModel(
            id = id,
            oauthId = oauthId.oAuthIdInfo,
            role = role.char,
            username = username.usernameInfo,
            email = email.emailInfo,
            createdAt = createdAt.toString(),
        )
}
