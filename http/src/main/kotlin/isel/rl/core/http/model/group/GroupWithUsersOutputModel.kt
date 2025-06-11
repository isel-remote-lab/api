package isel.rl.core.http.model.group

import isel.rl.core.domain.group.Group

data class GroupWithUsersOutputModel(
    val id: Int,
    val groupName: String?,
    val groupDescription: String?,
    val ownerId: Int,
    val createdAt: String,
) {
    companion object {
        fun mapOf(group: Group) =
            GroupWithUsersOutputModel(
                id = group.id,
                groupName = group.name.groupNameInfo,
                groupDescription = group.description.groupDescriptionInfo,
                ownerId = group.ownerId,
                createdAt = group.createdAt.toString(),
            )
    }
}
