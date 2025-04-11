package rl.domain.user

import kotlinx.datetime.Instant
import rl.domain.user.props.*

class User internal constructor(
    val id: Int,
    oauthId: OAuthId,
    role: Role,
    username: Username,
    email: Email,
    createdAt: Instant
) : UserType(oauthId, role, username, email, createdAt)