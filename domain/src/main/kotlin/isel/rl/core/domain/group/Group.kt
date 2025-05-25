package isel.rl.core.domain.group

import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.user.User
import kotlinx.datetime.Instant

data class Group(
    val id: Int = 0,
    val groupName: GroupName,
    val groupDescription: GroupDescription,
    val createdAt: Instant,
    val ownerId: Int,
    val groupUsers: List<User> = emptyList(),
) {
    companion object {
        const val ID_PROP = "id"
        const val GROUP_NAME_PROP = "group_name"
        const val GROUP_DESCRIPTION_PROP = "group_description"
        const val CREATED_AT_PROP = "created_at"
        const val OWNER_ID_PROP = "owner_id"
    }
}
