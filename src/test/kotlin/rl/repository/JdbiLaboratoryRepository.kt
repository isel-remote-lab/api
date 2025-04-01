package rl.repository

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rl.RepoUtils
import rl.TestClock
import rl.repositoryJdbi.JdbiLaboratoryRepository
import rl.repositoryJdbi.JdbiUserRepository
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class JdbiLaboratoryRepository {
    @Test
    fun `store laboratory and retrieve`(){
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val userCreatedAt = clock.now()
            val userId = userRepo.createUser(username, email, userCreatedAt)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDuration = 10.minutes  //repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val labId = laboratoryRepo.createLaboratory(labName, labDuration, labCreatedAt, userId)

            //then: retrieve laboratory by Id
            val labById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(labById) { "No laboratory retrieved from database" }
            assertEquals(labName, labById!!.labName)
            assertEquals(labDuration, labById.labDuration.minutes)
            assertEquals(labCreatedAt, labById.createdAt)
            assertEquals(userId, labById.ownerId)
            assertTrue(labById.id >= 0)

            // then: retrieving a laboratory by name
            val labByName = laboratoryRepo.getLaboratoryByName(labName)
            assertNotNull(labByName) { "No laboratory retrieved from database" }
            assertEquals(labName, labByName!!.labName)
            assertEquals(labDuration, labByName.labDuration.minutes)
            assertEquals(labCreatedAt, labByName.createdAt)
            assertEquals(userId, labByName.ownerId)
            assertTrue(labByName.id >= 0)
        }
    }

    @Test
    fun `update laboratory name and delete it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val userCreatedAt = clock.now()
            val userId = userRepo.createUser(username, email, userCreatedAt)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDuration = 10.minutes  //repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val labId = laboratoryRepo.createLaboratory(labName, labDuration, labCreatedAt, userId)

            // when: updating the laboratory name
            val newLabName = repoUtils.newTestLabName()
            assertTrue(laboratoryRepo.updateLaboratoryName(labId, newLabName))

            // then: retrieving the updated laboratory by name
            val updatedLabByName = laboratoryRepo.getLaboratoryByName(newLabName)
            assertNotNull(updatedLabByName) { "No updated laboratory retrieved from database" }
            assertEquals(newLabName, updatedLabByName!!.labName)

            // when: deleting the laboratory
            assertTrue(laboratoryRepo.deleteLaboratory(labId))

            // then: retrieving the deleted laboratory by Id should return null
            assertEquals(null, laboratoryRepo.getLaboratoryById(labId))
        }
    }

    @Test
    fun `add group to laboratory and remove it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val userCreatedAt = clock.now()
            val userId = userRepo.createUser(username, email, userCreatedAt)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDuration = 10.minutes  //repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val labId = laboratoryRepo.createLaboratory(labName, labDuration, labCreatedAt, userId)

            // when: adding a group to the laboratory
            val groupId = 1 // Replace with actual group ID
            assertTrue(laboratoryRepo.addGroupToLaboratory(labId, groupId))

            // then: retrieving the laboratory groups
            val groups = laboratoryRepo.getLaboratoryGroups(labId)
            assertNotNull(groups) { "No groups retrieved from database" }
            assertTrue(groups.contains(groupId)) { "Group not found in laboratory groups" }
            assertTrue(groups.size == 1) { "Unexpected number of groups in laboratory" }

            // when: removing the group from the laboratory
            assertTrue(laboratoryRepo.removeGroupFromLaboratory(labId, groupId))

            // then: retrieving the laboratory groups should be empty
            val groupsAfterRemoval = laboratoryRepo.getLaboratoryGroups(labId)
            assertNotNull(groupsAfterRemoval) { "No groups retrieved from database" }
            assertTrue(groupsAfterRemoval.isEmpty()) { "Groups should be empty after removal" }
        }
    }

    companion object {
        private val repoUtils = RepoUtils()
    }
}