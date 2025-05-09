package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.annotations.RequireApiKey
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.http.model.user.UserLoginInputModel
import isel.rl.core.http.model.user.UserLoginOutputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
data class AuthController(
    private val usersService: IUsersService,
) {
    @RequireApiKey
    @PostMapping(Uris.Auth.LOGIN)
    fun login(
        @RequestBody input: UserLoginInputModel,
    ): ResponseEntity<*> =
        when (
            val result = usersService.login(input.name, input.email)
        ) {
            is Success -> {
                val user = result.value.first
                val token = result.value.second

                val cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .build()

                ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(
                        SuccessResponse(
                            message = "User logged in successfully",
                            data = UserLoginOutputModel.mapOf(
                                token,
                                user
                            )
                        ),
                    )
            }

            is Failure -> handleServicesExceptions(result.value)
        }

    /**
     * Logs out the authenticated user.
     *
     * @param user the authenticated user
     */
    @PostMapping(Uris.Auth.LOGOUT)
    fun logout(user: AuthenticatedUser) {
        usersService.revokeToken(user.token)
    }
}
