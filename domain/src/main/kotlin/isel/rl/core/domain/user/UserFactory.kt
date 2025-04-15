package isel.rl.core.domain.user

import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import kotlinx.datetime.Instant

object UserFactory {
    fun createUser(
        id: Int,
        oauthId: OAuthId,
        role: Role,
        username: Username,
        email: Email,
        createdAt: Instant,
    ) = User(id, oauthId, role, username, email, createdAt)

    fun createValidatedUser(
        oauthId: OAuthId,
        role: Role,
        username: Username,
        email: Email,
        createdAt: Instant,
    ) = ValidatedUser(oauthId, role, username, email, createdAt)
}
