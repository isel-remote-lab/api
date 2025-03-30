package rl.repository

import kotlinx.datetime.Instant
import rl.domain.group.Group
import rl.domain.user.User

/**
 * Repository for groups
 */
interface GroupRepository {
    fun createGroup(
        groupName: String,
        groupDescription: String,
        createdAt: Instant,
        ownerId: Int
    ): Int

    fun getGroupById(groupId: Int): Group?

    fun getGroupByName(groupName: String): Group?

    fun getGroupUsers(groupId: Int): List<Int>

    fun addUserToGroup(userId: Int, groupId: Int): Boolean

    fun removeUserFromGroup(userId: Int, groupId: Int): Boolean

    fun updateGroupName(groupId: Int, groupName: String): Boolean

    fun updateGroupDescription(groupId: Int, groupDescription: String): Boolean

    fun deleteGroup(groupId: Int): Boolean
}