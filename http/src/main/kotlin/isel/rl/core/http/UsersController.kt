package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.user.UserCreateInputModel
import isel.rl.core.http.model.user.UserOutputModel
import isel.rl.core.services.UsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
data class UsersController(
    private val usersService: UsersService
) {
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> =
        when (
            val result = usersService.createUser(input.oauthId, input.role, input.username, input.email)
        ) {
            is Success -> {
                ResponseEntity.status(201).body(result.value)
            }

            is Failure -> {
                when (result.value) {
                    ServicesExceptions.Users.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
                    ServicesExceptions.Users.InvalidRole -> Problem.response(400, Problem.invalidRole)
                    ServicesExceptions.Users.InvalidUsername -> Problem.response(400, Problem.invalidUsername)
                    ServicesExceptions.Users.InvalidOauthId -> Problem.response(400, Problem.invalidOauthId)
                    else -> Problem.response(500, Problem.unexpectedBehaviour)
                }
            }
        }

    @GetMapping(Uris.Users.GET)
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserById(id)) {
            is Success -> {
                ResponseEntity.status(200).body(
                    UserOutputModel(
                        id = result.value.id.toString(),
                        oauthId = result.value.oauthId.oAuthIdInfo,
                        role = result.value.role.char,
                        username = result.value.username.usernameInfo,
                        email = result.value.email.emailInfo,
                        createdAt = result.value.createdAt.toString(),
                    ),
                )
            }

            is Failure -> {
                when (result.value) {
                    ServicesExceptions.Users.InvalidUserId -> Problem.response(400, Problem.invalidUserId)
                    ServicesExceptions.Users.UserNotFound -> Problem.response(404, Problem.userNotFound)
                    else -> Problem.response(500, Problem.unexpectedBehaviour)
                }
            }
        }

    @GetMapping(Uris.Users.GET_BY_EMAIL)
    fun getByEmail(
        @RequestParam email: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserByEmail(email)) {
            is Success -> {
                ResponseEntity.status(200).body(
                    UserOutputModel(
                        id = result.value.id.toString(),
                        oauthId = result.value.oauthId.oAuthIdInfo,
                        role = result.value.role.char,
                        username = result.value.username.usernameInfo,
                        email = result.value.email.emailInfo,
                        createdAt = result.value.createdAt.toString(),
                    ),
                )
            }

            is Failure -> {
                when (result.value) {
                    ServicesExceptions.Users.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
                    ServicesExceptions.Users.UserNotFound -> Problem.response(404, Problem.userNotFound)
                    else -> Problem.response(500, Problem.unexpectedBehaviour)
                }
            }
        }

    @GetMapping(Uris.Users.GET_BY_OAUTHID)
    fun getByOauthId(
        @PathVariable oauthid: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserByOAuthId(oauthid)) {
            is Success -> {
                ResponseEntity.status(200).body(
                    UserOutputModel(
                        id = result.value.id.toString(),
                        oauthId = result.value.oauthId.oAuthIdInfo,
                        role = result.value.role.char,
                        username = result.value.username.usernameInfo,
                        email = result.value.email.emailInfo,
                        createdAt = result.value.createdAt.toString(),
                    ),
                )
            }

            is Failure -> {
                when (result.value) {
                    ServicesExceptions.Users.InvalidEmail -> Problem.response(400, Problem.invalidOauthId)
                    ServicesExceptions.Users.UserNotFound -> Problem.response(404, Problem.userNotFound)
                    else -> Problem.response(500, Problem.unexpectedBehaviour)
                }
            }
        }
}