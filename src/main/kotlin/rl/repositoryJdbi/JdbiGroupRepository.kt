package rl.repositoryJdbi

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import rl.domain.group.Group
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.repository.GroupRepository

data class JdbiGroupRepository(
    val handle: Handle
) : GroupRepository {
    override fun createGroup(groupName: GroupName, groupDescription: GroupDescription, createdAt: Instant, ownerId: Int): Int =
        handle.createUpdate(
            """
           INSERT INTO rl.group (group_name, group_description, created_at, owner_id)
           VALUES (:group_name, :group_description, :created_at, :owner_id)
        """
        )
            .bind("group_name", groupName.groupNameInfo)
            .bind("group_description", groupDescription.groupDescriptionInfo)
            .bind("created_at", createdAt.toJavaInstant())
            .bind("owner_id", ownerId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
            .also { groupId ->
                addUserToGroup(ownerId, groupId)
            }


    override fun getGroupById(groupId: Int): Group? =
        handle.createQuery("""SELECT * FROM rl.group WHERE id = :id""")
            .bind("id", groupId)
            .mapTo<Group>()
            .singleOrNull()

    override fun getGroupByName(groupName: GroupName): Group? =
        handle.createQuery("""SELECT * FROM rl.group WHERE group_name = :group_name""")
            .bind("group_name", groupName.groupNameInfo)
            .mapTo<Group>()
            .singleOrNull()

    override fun getGroupUsers(groupId: Int): List<Int> =
        handle.createQuery(
            """
            SELECT user_id FROM rl.user_group 
            WHERE group_id = :group_id
        """
        )
            .bind("group_id", groupId)
            .mapTo<Int>()
            .list()


    override fun addUserToGroup(userId: Int, groupId: Int): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.user_group (user_id, group_id)
            VALUES (:user_id, :group_id)
        """
        )
            .bind("user_id", userId)
            .bind("group_id", groupId)
            .execute() == 1


    override fun removeUserFromGroup(userId: Int, groupId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.user_group
            WHERE user_id = :user_id AND group_id = :group_id
        """
        )
            .bind("user_id", userId)
            .bind("group_id", groupId)
            .execute() == 1

    override fun updateGroupName(groupId: Int, groupName: GroupName): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.group 
            SET group_name = :group_name 
            WHERE id = :id
        """
        )
            .bind("id", groupId)
            .bind("group_name", groupName.groupNameInfo)
            .execute() == 1

    override fun updateGroupDescription(groupId: Int, groupDescription: GroupDescription): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.group 
            SET group_description = :group_description 
            WHERE id = :id
        """
        )
            .bind("id", groupId)
            .bind("group_description", groupDescription.groupDescriptionInfo)
            .execute() == 1

    override fun deleteGroup(groupId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.group
            WHERE id = :id
        """
        )
            .bind("id", groupId)
            .execute() == 1
}