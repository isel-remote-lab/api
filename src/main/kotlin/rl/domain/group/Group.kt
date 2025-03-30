package rl.domain.group

import kotlinx.datetime.Instant

data class Group(
    val id: Int,
    val groupName: String,
    val groupDescription: String,
    val createdAt: Instant,
    val ownerId: Int,
)
