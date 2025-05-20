package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.GroupWithUsers
import isel.rl.core.utils.Either

typealias CreateGroupResult = Either<ServicesExceptions, Group>
typealias GetGroupByIdResult = Either<ServicesExceptions, GroupWithUsers>
typealias GetUserGroupsResult = Either<ServicesExceptions, List<Group>>
typealias AddUserToGroupResult = Either<ServicesExceptions, Unit>
typealias RemoveUserFromGroupResult = Either<ServicesExceptions, Unit>

interface IGroupsService {
    fun createGroup(
        groupName: String?,
        groupDescription: String?,
        ownerId: Int,
    ): CreateGroupResult

    fun getGroupById(groupId: String): GetGroupByIdResult

    fun getUserGroups(
        userId: String,
        limit: String? = null,
        skip: String? = null,
    ): GetUserGroupsResult

    fun addUserToGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): AddUserToGroupResult

    fun removeUserFromGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): RemoveUserFromGroupResult
}
