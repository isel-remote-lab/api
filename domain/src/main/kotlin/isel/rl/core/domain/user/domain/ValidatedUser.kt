package isel.rl.core.domain.user.domain

import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import kotlinx.datetime.Instant

data class ValidatedUser internal constructor(
    val oauthId: OAuthId,
    val role: Role,
    val username: Username,
    val email: Email,
    val createdAt: Instant,
)
