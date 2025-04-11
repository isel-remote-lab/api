package rl.domain.user

import kotlinx.datetime.Instant
import rl.domain.user.props.*

object UserFactory {
    fun createUser(
        id: Int,
        oauthId: OAuthId,
        role: Role,
        username: Username,
        email: Email,
        createdAt: Instant
    ) = User(id, oauthId, role, username, email, createdAt)

    fun createValidatedUser(
        oauthId: OAuthId,
        role: Role,
        username: Username,
        email: Email,
        createdAt: Instant
    ) = ValidatedUser(oauthId, role, username, email, createdAt)
}