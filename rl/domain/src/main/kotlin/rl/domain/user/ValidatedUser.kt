package rl.domain.user

import kotlinx.datetime.Instant

class ValidatedUser internal constructor(
    oauthId: OAuthId,
    role: Role,
    username: Username,
    email: Email,
    createdAt: Instant
) : UserType(oauthId, role, username, email, createdAt)