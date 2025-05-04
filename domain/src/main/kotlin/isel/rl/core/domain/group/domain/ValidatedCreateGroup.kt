package isel.rl.core.domain.group.domain

import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant

data class ValidatedCreateGroup internal constructor(
    val groupName: GroupName,
    val groupDescription: GroupDescription,
    val createdAt: Instant,
    val ownerId: Int,
)