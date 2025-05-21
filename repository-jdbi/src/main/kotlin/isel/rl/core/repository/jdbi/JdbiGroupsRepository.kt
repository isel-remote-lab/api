package isel.rl.core.repository.jdbi

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.repository.GroupsRepository
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

data class JdbiGroupsRepository(
    val handle: Handle,
) : GroupsRepository {
    override fun createGroup(validatedCreateGroup: Group): Int =
        handle.createUpdate(
            """
           INSERT INTO rl.group (group_name, group_description, created_at, owner_id)
           VALUES (:group_name, :group_description, :created_at, :owner_id)
        """,
        )
            .bind("group_name", validatedCreateGroup.groupName.groupNameInfo)
            .bind("group_description", validatedCreateGroup.groupDescription.groupDescriptionInfo)
            .bind("created_at", validatedCreateGroup.createdAt.toJavaInstant())
            .bind("owner_id", validatedCreateGroup.ownerId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
            .also {
                addUserToGroup(validatedCreateGroup.ownerId, it)
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
        """,
        )
            .bind("group_id", groupId)
            .mapTo<Int>()
            .list()

    override fun getUserGroups(
        userId: Int,
        limitAndSkip: LimitAndSkip,
    ): List<Group> =
        handle.createQuery(
            """
            SELECT g.* FROM rl.group g
            JOIN rl.user_group ug ON g.id = ug.group_id
            WHERE ug.user_id = :user_id
            LIMIT :limit OFFSET :skip
        """,
        )
            .bind("user_id", userId)
            .bind("limit", limitAndSkip.limit)
            .bind("skip", limitAndSkip.skip)
            .mapTo<Group>()
            .list()

    override fun checkIfGroupExists(groupId: Int): Boolean =
        handle.createQuery(
            """
            SELECT EXISTS (
                SELECT 1 
                FROM rl.group 
                WHERE id = :id
            )
        """,
        )
            .bind("id", groupId)
            .mapTo<Boolean>()
            .one()

    override fun getGroupOwnerId(groupId: Int): Int =
        handle.createQuery(
            """
            SELECT owner_id FROM rl.group 
            WHERE id = :id
        """,
        )
            .bind("id", groupId)
            .mapTo<Int>()
            .one()

    override fun addUserToGroup(
        userId: Int,
        groupId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.user_group (user_id, group_id)
            VALUES (:user_id, :group_id)
        """,
        )
            .bind("user_id", userId)
            .bind("group_id", groupId)
            .execute() == 1

    override fun removeUserFromGroup(
        userId: Int,
        groupId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.user_group
            WHERE user_id = :user_id AND group_id = :group_id
        """,
        )
            .bind("user_id", userId)
            .bind("group_id", groupId)
            .execute() == 1

    override fun updateGroup(
        groupId: Int,
        groupName: GroupName?,
        groupDescription: GroupDescription?,
    ): Boolean {
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
        """,
        )
            .bind("id", groupId)
            .execute() == 1
}
