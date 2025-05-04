package isel.rl.core.http.pipeline

import isel.rl.core.http.model.user.AuthenticatedUser
import isel.rl.core.security.JWTUtils
import isel.rl.core.services.UsersService
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import org.springframework.stereotype.Component

@Component
class RequestJWTProcessor(
    private val usersService: UsersService,
    private val jwtUtils: JWTUtils,
) {
    fun processAuthorizationCookieValue(jwt: String?): AuthenticatedUser? {
        if (jwt == null) {
            return null
        }

        val userId = jwtUtils.validateJWTTokenAndRetrieveUserId(jwt)
        val user = usersService.getUserById(userId)
        return if (user is Failure) {
            null
        } else {
            val userDetails = (user as Success).value
            AuthenticatedUser(
                id = userDetails.id,
                oauthId = userDetails.oAuthId,
                role = userDetails.role,
                username = userDetails.username,
                email = userDetails.email,
                createdAt = userDetails.createdAt,
            )
        }
    }

    companion object {
        const val SCHEME = "Bearer"
    }
}
