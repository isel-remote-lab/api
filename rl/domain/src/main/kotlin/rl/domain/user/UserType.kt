package rl.domain.user

import kotlinx.datetime.Instant
import rl.domain.user.props.Email
import rl.domain.user.props.OAuthId
import rl.domain.user.props.Role
import rl.domain.user.props.Username

sealed class UserType(
    val oauthId: OAuthId,
    val role: Role,
    val username: Username,
    val email: Email,
    val createdAt: Instant
)