package isel.rl.core.http.model.group

import isel.rl.core.domain.group.Group
import isel.rl.core.http.model.user.UserOutputModel

data class GroupWithUsersOutputModel(
    val id: Int,
    val groupName: String?,
    val groupDescription: String?,
    val ownerId: Int,
    val createdAt: String,
    val users: List<UserOutputModel>,
) {
    companion object {
        fun mapOf(group: Group) =
            GroupWithUsersOutputModel(
                id = group.id,
                groupName = group.groupName.groupNameInfo,
                groupDescription = group.groupDescription.groupDescriptionInfo,
                ownerId = group.ownerId,
                createdAt = group.createdAt.toString(),
                users =
                    group.groupUsers.map { user ->
                        UserOutputModel(
                            id = user.id,
                            role = user.role.char,
                            name = user.name.nameInfo,
                            email = user.email.emailInfo,
                            createdAt = user.createdAt.toString(),
                        )
                    },
            )
    }
}
