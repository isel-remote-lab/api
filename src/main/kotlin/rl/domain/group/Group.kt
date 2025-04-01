package rl.domain.group

import kotlinx.datetime.Instant

data class Group(
    val id: Int,
    val groupName: GroupName,
    val groupDescription: GroupDescription,
    val createdAt: Instant,
    val ownerId: Int,
)
