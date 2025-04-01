package rl.repository

import kotlinx.datetime.Instant
import rl.domain.group.Group
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.domain.user.User

/**
 * Repository for groups
 */
interface GroupRepository {
    fun createGroup(
        groupName: GroupName,
        groupDescription: GroupDescription,
        createdAt: Instant,
        ownerId: Int
    ): Int

    fun getGroupById(groupId: Int): Group?

    fun getGroupByName(groupName: GroupName): Group?

    fun getGroupUsers(groupId: Int): List<Int>

    fun addUserToGroup(userId: Int, groupId: Int): Boolean

    fun removeUserFromGroup(userId: Int, groupId: Int): Boolean

    fun updateGroupName(groupId: Int, groupName: GroupName): Boolean

    fun updateGroupDescription(groupId: Int, groupDescription: GroupDescription): Boolean

    fun deleteGroup(groupId: Int): Boolean
}