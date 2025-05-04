package isel.rl.core.http.pipeline.interceptors

import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.services.UsersService
import org.springframework.stereotype.Component

@Component
class RequestTokenProcessor(
    val usersService: UsersService,
) {
    fun processAuthorizationCookieValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }

        return usersService.getUserByToken(authorizationValue)?.let {
            AuthenticatedUser(
                it,
                authorizationValue,
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}