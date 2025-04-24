package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.User
import isel.rl.core.http.model.user.UserLoginInputModel
import isel.rl.core.http.model.user.UserOutputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
data class UsersController(
    private val usersService: IUsersService,
) {
    @RequireApiKey
    @PostMapping(Uris.Users.LOGIN)
    fun login(
        @RequestBody input: UserLoginInputModel,
    ): ResponseEntity<*> =
        when (
            val result = usersService.login(input.oauthId, input.username, input.email, input.accessToken)
        ) {
            is Success -> {
                val cookie =
                    ResponseCookie.from("session", result.value)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Strict")
                        .path("/")
                        .build()

                ResponseEntity.status(200)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build<Unit>()
                // .body(UserTokenCreateOutputModel(res.value.tokenValue))
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    /*
    @RequireApiKey
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> =
        when (
            val result = usersService.createUser(input.oauthId, input.role, input.username, input.email)
        ) {
            is Success -> {
                ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            }

            is Failure -> handleServicesExceptions(result.value)
        }

     */

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

    /*@RequireApiKey
    @GetMapping(Uris.Users.GET_BY_OAUTHID)
    fun getByOAuthID(
        @RequestParam oauthid: String,
    ): ResponseEntity<*> =
        when (val result = usersService.getUserByEmailOrAuthId(oauthid)) {
            is Success -> {
                ResponseEntity.status(HttpStatus.OK).body(
                    mapOf("user" to result.value.toUserOutput())
                )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

     */

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
