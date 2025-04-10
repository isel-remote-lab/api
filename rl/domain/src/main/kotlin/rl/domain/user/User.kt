package rl.domain.user

import kotlinx.datetime.Instant

class User internal constructor(
    val id: Int,
    oauthId: OAuthId,
    role: Role,
    username: Username,
    email: Email,
    createdAt: Instant
) : UserType(oauthId, role, username, email, createdAt)