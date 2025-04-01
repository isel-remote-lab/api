package rl.domain.user

import kotlinx.datetime.Instant

data class User(
    val id: Int,
    val username: Username,
    val email: Email,
    val createdAt: Instant
)