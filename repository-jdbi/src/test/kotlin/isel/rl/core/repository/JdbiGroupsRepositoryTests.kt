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
    fun `store group and retrieve`() {
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

            // when: retrieving a group by Id
            val groupById = groupRepo.getGroupById(groupId)

            // then: verify the retrieved group details
            initialGroup.assertGroupWith(groupById)

            // when: retrieving a group by name
            val groupByName = groupRepo.getGroupByName(initialGroup.groupName)

            // then: verify the retrieved group details
            initialGroup.assertGroupWith(groupByName)
        }
    }

    @Test
    fun `add user to group, verify and remove`() {
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

            // then: verify the user is in the group
            val groupUsers = groupRepo.getGroupUsers(groupId)
            assertTrue(groupUsers.size == 2, "Expected group to have 2 user but had ${groupUsers.size}")
            assertTrue(groupUsers.contains(userId), "Expected group to have user $userId but it didn't")
            assertTrue(groupUsers.contains(ownerId), "Expected group to have user $ownerId but it didn't")

            // when: Removing user from group
            assertTrue(
                groupRepo.removeUserFromGroup(userId, groupId),
                "Failed to remove user $userId from group $groupId",
            )

            // then: verify the user is not in the group anymore
            val groupUsers2 = groupRepo.getGroupUsers(groupId)
            assertTrue(groupUsers2.size == 1, "Expected group to have 1 users but had ${groupUsers2.size}")
            assertTrue(
                !groupUsers2.contains(userId),
                "Expected group to not have user $userId but it didn't",
            )
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
            assertEquals(newGroupName, group.groupName, "Group name does not match")
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
            assertEquals(newGroupDescription, group.groupDescription, "Group description does not match")
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
            assertEquals(newGroupName, group.groupName, "Group name does not match")
            assertEquals(newGroupDescription, group.groupDescription, "Group description does not match")
        }
    }

    /*@Test
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

            // then: delete group
            assertTrue(groupRepo.deleteGroup(groupId), "Failed to delete group $groupId")

            // then: try to retrieve it
            assertNull(groupRepo.getGroupById(groupId), "Group $groupId was not deleted")
        }
    }*/

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
            assertEquals(groupName, group.groupName)
            assertEquals(groupDescription, group.groupDescription)
            assertEquals(createdAt, group.createdAt)
            assertEquals(ownerId, group.ownerId)
            assertTrue(group.id >= 0)
        }
    }
}
