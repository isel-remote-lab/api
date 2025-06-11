package isel.rl.core.repository

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.repository.jdbi.JdbiGroupsRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbiGroupsRepositoryTests {
    @Test
    fun `create group with valid parameters`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: creating a valid group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: verify the created group details
            initialGroup.assertGroupWith(groupRepo.getGroupById(groupId))
        }
    }

    @Test
    fun `get group by id`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: retrieve the group by id
            initialGroup.assertGroupWith(groupRepo.getGroupById(groupId))
        }
    }

    @Test
    fun `get group by name`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            groupRepo.createGroup(initialGroup)

            // then: retrieve the group by name
            initialGroup.assertGroupWith(groupRepo.getGroupByName(initialGroup.groupName))
        }
    }

    @Test
    fun `get group owner id`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: retrieve the group owner id
            assertEquals(ownerId, groupRepo.getGroupOwnerId(groupId))
        }
    }

    @Test
    fun `check if group exists`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: check if the group exists
            assertTrue(groupRepo.checkIfGroupExists(groupId), "Expected group $groupId to exist")
        }
    }

    @Test
    fun `add user to group`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: add user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId), "Failed to add user $userId to group $groupId")
        }
    }

    @Test
    fun `get user groups`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // when: Adding a user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId))

            // when: storing another group
            val initialGroup2 = InitialGroup(clock, ownerId)
            val groupId2 = groupRepo.createGroup(initialGroup2)

            // when: Adding a user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId2))

            // then: get user groups
            val userGroups = groupRepo.getUserGroups(userId, LimitAndSkip())
            assertTrue(userGroups.size == 2, "Expected user to have 2 groups but had ${userGroups.size}")
        }
    }

    @Test
    fun `check if user is in group`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // when: Adding a user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId))

            // then: check if the user is in the group
            assertTrue(groupRepo.checkIfUserIsInGroup(userId, groupId), "Expected user $userId to be in group $groupId")
        }
    }

    @Test
    fun `get group users`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // when: Adding a user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId))

            // then: get group users
            val groupUsers = groupRepo.getGroupUsers(groupId, LimitAndSkip())
            assertTrue(groupUsers.size == 2, "Expected group to have 1 user but had ${groupUsers.size}")
        }
    }

    @Test
    fun `remove user from group`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // when: Adding a user to the group
            assertTrue(groupRepo.addUserToGroup(userId, groupId))

            // then: remove user from the group
            assertTrue(groupRepo.removeUserFromGroup(userId, groupId), "Failed to remove user $userId from group $groupId")

            // then: check if the user is still in the group
            assertTrue(!groupRepo.checkIfUserIsInGroup(userId, groupId), "Expected user $userId to not be in group $groupId")
        }
    }

    @Test
    fun `update group name`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: update group name
            val newGroupName = repoUtils.newTestGroupName()
            assertTrue(groupRepo.updateGroup(groupId, newGroupName), "Failed to update group name")

            // then: check for the new name
            val group = groupRepo.getGroupById(groupId)
            assertNotNull(group, "No group retrieved")
            assertEquals(newGroupName, group.name, "Group name does not match")
        }
    }

    @Test
    fun `update group description`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: update group description
            val newGroupDescription = repoUtils.newTestGroupDescription()
            assertTrue(groupRepo.updateGroup(groupId, null, newGroupDescription), "Failed to update group description")

            // then: check for the new description
            val group = groupRepo.getGroupById(groupId)
            assertNotNull(group, "No group retrieved")
            assertEquals(newGroupDescription, group.description, "Group description does not match")
        }
    }

    @Test
    fun `update group name and description`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: update group name and description
            val newGroupName = repoUtils.newTestGroupName()
            val newGroupDescription = repoUtils.newTestGroupDescription()
            assertTrue(
                groupRepo.updateGroup(groupId, newGroupName, newGroupDescription),
                "Failed to update group name and description",
            )

            // then: check for the new name and description
            val group = groupRepo.getGroupById(groupId)
            assertNotNull(group, "No group retrieved")
            assertEquals(newGroupName, group.name, "Group name does not match")
            assertEquals(newGroupDescription, group.description, "Group description does not match")
        }
    }

    @Test
    fun `delete group`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupsRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val initialGroup = InitialGroup(clock, ownerId)
            val groupId = groupRepo.createGroup(initialGroup)

            // then: delete the group
            assertTrue(groupRepo.deleteGroup(groupId), "Failed to delete group $groupId")

            // then: check if the group exists
            assertTrue(!groupRepo.checkIfGroupExists(groupId), "Expected group $groupId to not exist after deletion")
        }
    }

    companion object {
        private val repoUtils = RepoUtils()

        private data class InitialGroup(
            val clock: TestClock,
            val ownerId: Int,
            val groupName: GroupName = repoUtils.newTestGroupName(),
            val groupDescription: GroupDescription = repoUtils.newTestGroupDescription(),
            val createdAt: Instant = clock.now(),
        )

        private fun JdbiGroupsRepository.createGroup(group: InitialGroup): Int =
            createGroup(
                repoUtils.groupsDomain.validateCreateGroup(
                    group.groupName.groupNameInfo,
                    group.groupDescription.groupDescriptionInfo,
                    group.createdAt,
                    group.ownerId,
                ),
            )

        private fun InitialGroup.assertGroupWith(group: Group?) {
            assertNotNull(group) { "No group retrieved" }
            assertEquals(groupName, group.name)
            assertEquals(groupDescription, group.description)
            assertEquals(createdAt, group.createdAt)
            assertEquals(ownerId, group.ownerId)
            assertTrue(group.id >= 0)
        }
    }
}
