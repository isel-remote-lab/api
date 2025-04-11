package rl.domain.user

import kotlinx.datetime.Instant
import rl.domain.user.props.Email
import rl.domain.user.props.OAuthId
import rl.domain.user.props.Role
import rl.domain.user.props.Username

class ValidatedUser internal constructor(
    oauthId: OAuthId,
    role: Role,
    username: Username,
    email: Email,
    createdAt: Instant
) : UserType(oauthId, role, username, email, createdAt)