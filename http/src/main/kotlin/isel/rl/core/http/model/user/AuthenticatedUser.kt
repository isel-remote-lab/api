package isel.rl.core.http.model.user

import isel.rl.core.domain.user.User

data class AuthenticatedUser(
    val user: User,
)
