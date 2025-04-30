package isel.rl.core.http

import isel.rl.core.domain.Uris
import isel.rl.core.http.model.user.UserLoginInputModel
import isel.rl.core.http.utils.handleServicesExceptions
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.http.HttpHeaders
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
            val result = usersService.login(input.oauthId, input.username, input.email, input.accessToken)
        ) {
            is Success -> {
                val cookie =
                    ResponseCookie.from("jwt-token", result.value)
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
}
