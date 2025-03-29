package rl.repository

import kotlinx.datetime.Instant
import rl.domain.group.Group

/**
 * Repository for groups
 */
interface GroupRepository {
    fun createGroup(
        groupName: String,
        groupDescription: String,
        createdAt: Instant,
        ownerId: Int
    ): Group

    fun getGroupById(groupId: Int): Group

    fun getGroupsByName(groupName: String): Group

    fun updateGroupName(groupId: Int, groupName: String): Group

    fun updateGroupDescription(groupId: Int, groupDescription: String): Group
}