package isel.rl.core.domain.group

import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant

data class Group(
    val id: Int,
    val groupName: GroupName,
    val groupDescription: GroupDescription,
    val createdAt: Instant,
    val ownerId: Int,
)
