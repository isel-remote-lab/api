package isel.rl.core.repository

import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.domain.ValidatedCreateGroup
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName

/**
 * Repository for groups
 */
interface GroupsRepository {
    fun createGroup(
        validatedCreateGroup: ValidatedCreateGroup
    ): Int

    fun getGroupById(groupId: Int): Group?

    fun getGroupByName(groupName: GroupName): Group?

    fun getGroupUsers(groupId: Int): List<Int>

    fun addUserToGroup(
        userId: Int,
        groupId: Int,
    ): Boolean

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
