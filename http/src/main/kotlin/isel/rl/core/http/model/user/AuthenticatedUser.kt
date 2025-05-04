package isel.rl.core.http.model.user

import isel.rl.core.domain.user.User

/**
 * Represents an authenticated user.
 *
 * @property user the user
 * @property token the authentication token
 */
data class AuthenticatedUser(
    val user: User,
    val token: String,
)
