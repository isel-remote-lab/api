package isel.rl.core.domain.user

import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import kotlinx.datetime.Instant

class ValidatedUser internal constructor(
    oauthId: OAuthId,
    role: Role,
    username: Username,
    email: Email,
    createdAt: Instant,
) : UserType(oauthId, role, username, email, createdAt)
