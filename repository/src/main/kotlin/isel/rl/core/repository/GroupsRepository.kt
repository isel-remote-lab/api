package isel.rl.core.repository

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName

/**
 * Repository for groups
 */
interface GroupsRepository {
    fun createGroup(validatedCreateGroup: Group): Int

    fun getGroupById(groupId: Int): Group?

    fun getGroupByName(groupName: GroupName): Group?

    fun getGroupOwnerId(groupId: Int): Int?

    fun checkIfGroupExists(groupId: Int): Boolean

    fun addUserToGroup(
        userId: Int,
        groupId: Int,
    ): Boolean

    fun getUserGroups(
        userId: Int,
        limitAndSkip: LimitAndSkip,
    ): List<Group>

    fun checkIfUserIsInGroup(
        userId: Int,
        groupId: Int,
    ): Boolean

    fun getGroupUsers(
        groupId: Int,
        limitAndSkip: LimitAndSkip,
    ): List<Int>

    fun removeUserFromGroup(
        userId: Int,
        groupId: Int,
    ): Boolean

    fun updateGroup(
        groupId: Int,
        groupName: GroupName? = null,
        groupDescription: GroupDescription? = null,
    ): Boolean

    fun deleteGroup(groupId: Int): Boolean
}
