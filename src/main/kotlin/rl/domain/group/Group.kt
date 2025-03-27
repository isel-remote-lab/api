package rl.domain.group

import kotlinx.datetime.Instant

data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: Instant,
    val ownerId: Int,
)
