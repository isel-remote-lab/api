package rl.repository

import rl.TestClock
import rl.repositoryJdbi.JdbiGroupRepository
import kotlin.test.*

class JdbiGroupRepositoryTests {
    @Test
    fun `store group and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val groupName = repoUtils.newTestGroupName()
            val groupDescription = repoUtils.newTestGroupDescription()
            val groupCreatedAt = clock.now()

            val groupId = groupRepo.createGroup(groupName, groupDescription, groupCreatedAt, ownerId)

            // when: retrieving a group by Id
            val groupById = groupRepo.getGroupById(groupId)

            // then:
            assertNotNull(groupById) { "No group retrieved from database" }
            assertEquals(groupName, groupById.groupName)
            assertEquals(groupDescription, groupById.groupDescription)
            assertEquals(groupCreatedAt, groupById.createdAt)
            assertTrue(groupById.id >= 0)

            // when: retrieving a group by name
            val groupByName = groupRepo.getGroupByName(groupName)

            // then:
            assertNotNull(groupByName) { "No group retrieved from database" }
            assertEquals(groupName, groupByName.groupName)
            assertEquals(groupDescription, groupByName.groupDescription)
            assertEquals(groupCreatedAt, groupByName.createdAt)
            assertTrue(groupByName.id >= 0)

            // then: verify if user is in user_group relation
            val groupUsers = groupRepo.getGroupUsers(groupId)
            assertTrue(groupUsers.size == 1)
            assertTrue(groupUsers.contains(ownerId))
        }
    }

    @Test
    fun `add user to group, verify and remove`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a group
            val groupName = repoUtils.newTestGroupName()
            val groupDescription = repoUtils.newTestGroupDescription()
            val groupCreatedAt = clock.now()
            val groupId = groupRepo.createGroup(groupName, groupDescription, groupCreatedAt, ownerId)

            // when: Adding a user to group
            assertTrue(groupRepo.addUserToGroup(userId, groupId))

            // then:
            val groupUsers = groupRepo.getGroupUsers(groupId)
            assertTrue(groupUsers.size == 2)
            assertTrue(groupUsers.contains(ownerId))
            assertTrue(groupUsers.contains(userId))

            // when: Removing user from group
            assertTrue(groupRepo.removeUserFromGroup(userId, groupId))

            //then:
            val groupUsers2 = groupRepo.getGroupUsers(groupId)
            assertTrue(groupUsers2.size == 1)
            assertTrue(groupUsers2.contains(ownerId))
        }
    }

    @Test
    fun `update group name and description`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val groupName = repoUtils.newTestGroupName()
            val groupDescription = repoUtils.newTestGroupDescription()
            val groupCreatedAt = clock.now()
            val groupId = groupRepo.createGroup(groupName, groupDescription, groupCreatedAt, ownerId)

            // then: update group name
            val newGroupName = repoUtils.newTestGroupName()
            assertTrue(groupRepo.updateGroupName(groupId, newGroupName))

            // then: update group description
            val newGroupDescription = repoUtils.newTestGroupDescription()
            assertTrue(groupRepo.updateGroupDescription(groupId, newGroupDescription))

            // then: check for the new name and description
            val group = groupRepo.getGroupById(groupId)
            assertNotNull(group)
            assertEquals(newGroupName, group.groupName)
            assertEquals(newGroupDescription, group.groupDescription)
        }
    }

    @Test
    fun `delete group`() {
        repoUtils.runWithHandle { handle ->
            // given: a group and user repo
            val groupRepo = JdbiGroupRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val ownerId = repoUtils.createTestUser(handle)

            // when: storing a group
            val groupName = repoUtils.newTestGroupName()
            val groupDescription = repoUtils.newTestGroupDescription()
            val groupCreatedAt = clock.now()
            val groupId = groupRepo.createGroup(groupName, groupDescription, groupCreatedAt, ownerId)

            assertTrue(groupRepo.getGroupUsers(groupId).size == 1)

            // when: removing the only user in the group
            assertTrue(groupRepo.removeUserFromGroup(ownerId, groupId))

            // then: delete group
            assertTrue(groupRepo.deleteGroup(groupId))

            // then: try to retrieve it
            assertNull(groupRepo.getGroupById(groupId))
        }
    }

    companion object {
        private val repoUtils = RepoUtils()
    }
}