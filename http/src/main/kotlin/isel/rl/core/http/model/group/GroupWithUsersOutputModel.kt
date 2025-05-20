package isel.rl.core.http.model.group

import isel.rl.core.domain.group.GroupWithUsers
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
        fun mapOf(group: GroupWithUsers) =
            GroupWithUsersOutputModel(
                id = group.group.id,
                groupName = group.group.groupName.groupNameInfo,
                groupDescription = group.group.groupDescription.groupDescriptionInfo,
                ownerId = group.group.ownerId,
                createdAt = group.group.createdAt.toString(),
                users =
                    group.users.map { user ->
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
