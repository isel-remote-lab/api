package isel.rl.core.domain.group

import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import kotlinx.datetime.Instant

/**
 * Represents a group in the system.
 * A group is identified by its unique ID and contains a name, description, creation timestamp,
 * and the ID of its owner.
 */
data class Group(
    val id: Int = 0,
    val name: GroupName,
    val description: GroupDescription,
    val createdAt: Instant,
    val ownerId: Int,
) {
    companion object {
        const val ID_PROP = "id"
        const val GROUP_NAME_PROP = "name"
        const val GROUP_DESCRIPTION_PROP = "description"
        const val CREATED_AT_PROP = "created_at"
        const val OWNER_ID_PROP = "owner_id"
    }
}
