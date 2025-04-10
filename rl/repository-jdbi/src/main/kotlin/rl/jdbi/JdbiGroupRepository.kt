package rl.jdbi

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

    override fun updateGroup(groupId: Int, groupName: GroupName?, groupDescription: GroupDescription?): Boolean {
        val updateQuery = StringBuilder("UPDATE rl.group SET ")
        val params = mutableMapOf<String, Any?>()

        groupName?.let {
            updateQuery.append("group_name = :group_name, ")
            params["group_name"] = it.groupNameInfo
        }

        groupDescription?.let {
            updateQuery.append("group_description = :group_description, ")
            params["group_description"] = it.groupDescriptionInfo
        }

        updateQuery.delete(updateQuery.length - 2, updateQuery.length)
        updateQuery.append(" WHERE id = :id")
        params["id"] = groupId

        return handle.createUpdate(updateQuery.toString())
            .bindMap(params)
            .execute() == 1
    }

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