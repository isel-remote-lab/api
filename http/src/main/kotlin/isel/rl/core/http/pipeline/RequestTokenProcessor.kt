package isel.rl.core.http.pipeline

import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.services.UsersService
import org.springframework.stereotype.Component

@Component
class RequestTokenProcessor(
    val usersService: UsersService,
) {
    fun processAuthorizationValue(authorizationValue: String?, isCookie: Boolean): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }

        if (!isCookie) {
            val parts = authorizationValue.trim().split(" ")
            if (parts.size != 2) {
                return null
            }
            if (parts[0].lowercase() != SCHEME) {
                return null
            }
            return usersService.getUserByToken(parts[1])?.let {
                AuthenticatedUser(
                    it,
                    parts[1],
                )
            }
        } else {
            return usersService.getUserByToken(authorizationValue)?.let {
                AuthenticatedUser(
                    it,
                    authorizationValue,
                )
            }
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
