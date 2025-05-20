package isel.rl.core.http.model.group

import isel.rl.core.domain.group.Group

data class GroupOutputModel(
    val id: Int,
    val groupName: String?,
    val groupDescription: String?,
    val ownerId: Int,
    val createdAt: String,
) {
    companion object {
        fun mapOf(group: Group) =
            GroupOutputModel(
                id = group.id,
                groupName = group.groupName.groupNameInfo,
                groupDescription = group.groupDescription.groupDescriptionInfo,
                ownerId = group.ownerId,
                createdAt = group.createdAt.toString(),
            )
    }
}
