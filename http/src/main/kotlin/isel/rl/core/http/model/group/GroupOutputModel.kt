package isel.rl.core.http.model.group

import isel.rl.core.domain.group.Group

data class GroupOutputModel(
    val id: Int,
    val name: String?,
    val description: String?,
    val ownerId: Int,
    val createdAt: String,
) {
    companion object {
        fun mapOf(group: Group) =
            GroupOutputModel(
                id = group.id,
                name = group.name.groupNameInfo,
                description = group.description.groupDescriptionInfo,
                ownerId = group.ownerId,
                createdAt = group.createdAt.toString(),
            )
    }
}
