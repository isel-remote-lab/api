package isel.rl.core.services.utils

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.repository.jdbi.transaction.JdbiTransactionManager
import isel.rl.core.services.GroupsService
import isel.rl.core.services.TestClock
import isel.rl.core.services.utils.ServicesUtils.domainConfigs
import isel.rl.core.services.utils.ServicesUtils.groupsDomain
import isel.rl.core.services.utils.ServicesUtils.jdbi
import isel.rl.core.services.utils.ServicesUtils.usersDomain
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object GroupsServicesUtils {
    fun createGroupsServices(testClock: TestClock): GroupsService =
        GroupsService(
            JdbiTransactionManager(jdbi),
            testClock,
            groupsDomain,
            usersDomain,
        )

    data class InitialGroup(
        val id: Int = 0,
        val name: String = newTestGroupName(),
        val description: String = newTestGroupDescription(),
        val createdAt: Instant = Instant.DISTANT_PAST,
        val users: List<Int> = emptyList(),
        val ownerId: Int = 1,
    )

    fun createGroup(
        groupsService: GroupsService,
        initialGroup: InitialGroup = InitialGroup(),
        owner: User = User(initialGroup.ownerId, Role.TEACHER, Name(""), Email(""), Instant.DISTANT_PAST),
        expectedServiceException: KClass<*>? = null,
    ): InitialGroup {
        val group =
            groupsService.createGroup(
                initialGroup.name,
                initialGroup.description,
                owner,
            )

        if (expectedServiceException != null) {
            assertTrue(group is Failure, "Expected a failure, but got: $group")
            assertTrue(
                expectedServiceException.isInstance(group.value),
                "Expected a ServiceException, but got: ${group.value}",
            )
            return initialGroup
        }
        assertTrue(group is Success, "Expected a successful group creation, but got: $group")
        assertEquals(
            initialGroup.name,
            group.value.groupName.groupNameInfo,
            "Group name does not match expected value.",
        )
        assertEquals(
            initialGroup.description,
            group.value.groupDescription.groupDescriptionInfo,
            "Group description does not match expected value.",
        )
        return initialGroup.copy(
            id = group.value.id,
            createdAt = group.value.createdAt,
            users = listOf(group.value.ownerId),
        )
    }

    fun getGroupById(
        groupsService: GroupsService,
        expectedGroup: InitialGroup,
    ): InitialGroup {
        val group = groupsService.getGroupById(expectedGroup.id.toString())
        assertTrue(group is Success, "Expected a successful group retrieval, but got: $group")
        return assertGroup(
            expectedGroup,
            InitialGroup(
                id = group.value.id,
                name = group.value.groupName.groupNameInfo,
                description = group.value.groupDescription.groupDescriptionInfo,
                createdAt = group.value.createdAt,
                users = group.value.groupUsers.map { it.id },
                ownerId = group.value.ownerId,
            ),
        )
    }

    fun getGroupById(
        groupsService: GroupsService,
        groupId: String,
        expectedServiceException: KClass<*>,
    ) {
        val group = groupsService.getGroupById(groupId)
        assertTrue(group is Failure, "Expected a failure, but got: $group")
        assertTrue(
            expectedServiceException.isInstance(group.value),
            "Expected a ServiceException, but got: ${group.value}",
        )
    }

    fun getUserGroups(
        groupsService: GroupsService,
        userId: String,
        limit: String? = null,
        skip: String? = null,
        expectedGroups: List<InitialGroup> = emptyList(),
        expectedServiceException: KClass<*>? = null,
    ) {
        val userGroups = groupsService.getUserGroups(userId, limit, skip)

        if (expectedServiceException != null) {
            assertTrue(userGroups is Failure, "Expected a failure, but got: $userGroups")
            assertTrue(
                expectedServiceException.isInstance(userGroups.value),
                "Expected a ServiceException, but got: ${userGroups.value}",
            )
            return
        }
        assertTrue(userGroups is Success, "Expected a successful user groups retrieval, but got: $userGroups")
        assertEquals(
            expectedGroups.size,
            userGroups.value.size,
            "User groups size does not match expected value.",
        )
        expectedGroups.forEach { expectedGroup ->
            assertTrue(
                userGroups.value.any { it.id == expectedGroup.id },
                "Expected group with ID ${expectedGroup.id} not found in user groups.",
            )
        }
    }

    fun addUserToGroup(
        groupsService: GroupsService,
        actorUserId: Int,
        userId: String?,
        groupId: String,
        expectedServiceException: KClass<*>? = null,
    ) {
        val result = groupsService.addUserToGroup(actorUserId, userId, groupId)

        if (expectedServiceException != null) {
            assertTrue(result is Failure, "Expected a failure, but got: $result")
            assertTrue(
                expectedServiceException.isInstance(result.value),
                "Expected a ServiceException, but got: ${result.value}",
            )
            return
        }
        assertTrue(result is Success, "Expected a successful user addition to group, but got: $result")
    }

    fun removeUserFromGroup(
        groupsService: GroupsService,
        actorUserId: Int,
        userId: String?,
        groupId: String,
        expectedServiceException: KClass<*>? = null,
    ) {
        val result = groupsService.removeUserFromGroup(actorUserId, userId, groupId)

        if (expectedServiceException != null) {
            assertTrue(result is Failure, "Expected a failure, but got: $result")
            assertTrue(
                expectedServiceException.isInstance(result.value),
                "Expected a ServiceException, but got: ${result.value}",
            )
            return
        }
        assertTrue(result is Success, "Expected a successful user removal from group, but got: $result")
    }

    fun deleteGroup(
        groupsService: GroupsService,
        actorUserId: Int,
        groupId: String,
        expectedServiceException: KClass<*>? = null,
    ) {
        val result = groupsService.deleteGroup(actorUserId, groupId)

        if (expectedServiceException != null) {
            assertTrue(result is Failure, "Expected a failure, but got: $result")
            assertTrue(
                expectedServiceException.isInstance(result.value),
                "Expected a ServiceException, but got: ${result.value}",
            )
            return
        }
        assertTrue(result is Success, "Expected a successful group deletion, but got: $result")
    }

    val isGroupDescriptionOptional = domainConfigs.group.groupDescription.optional

    /**
     * Generates a random group name for testing purposes.
     */
    fun newTestGroupName() = "group-${abs(Random.nextLong())}"

    fun newTestInvalidGroupNameMax() = "a".repeat(domainConfigs.group.groupName.max + 1)

    fun newTestInvalidGroupNameMin() = ""

    /**
     * Generates a random group description for testing purposes.
     */
    fun newTestGroupDescription() = "description-${abs(Random.nextLong())}"

    fun newTestInvalidGroupDescriptionMax() = "a".repeat(domainConfigs.group.groupDescription.max + 1)

    fun newTestInvalidGroupDescriptionMin() = ""

    fun assertGroup(
        expectedGroup: InitialGroup,
        actualGroup: InitialGroup,
    ): InitialGroup {
        assertEquals(expectedGroup.id, actualGroup.id, "Group ID does not match expected value.")
        assertEquals(expectedGroup.name, actualGroup.name, "Group name does not match expected value.")
        assertEquals(
            expectedGroup.description,
            actualGroup.description,
            "Group description does not match expected value.",
        )
        assertEquals(
            expectedGroup.createdAt,
            actualGroup.createdAt,
            "Group creation date does not match expected value.",
        )
        assertEquals(
            expectedGroup.users.size,
            actualGroup.users.size,
            "Group user list size does not match expected value.",
        )
        expectedGroup.users.forEach {
            assertTrue(
                actualGroup.users.any { id -> id == it },
                "Expected user with ID $it not found in group users.",
            )
        }
        assertEquals(expectedGroup.ownerId, actualGroup.ownerId, "Group owner ID does not match expected value.")
        return actualGroup
    }
}
