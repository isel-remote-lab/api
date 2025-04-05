package rl.repository


import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rl.TestClock
import rl.repositoryJdbi.JdbiLaboratoryRepository
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class JdbiLaboratoryRepositoryTests {
    @Test
    fun `store laboratory and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            //then: retrieve laboratory by Id
            val labById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(labById) { "No laboratory retrieved from database" }
            assertEquals(labName, labById!!.labName)
            assertEquals(labDescription, labById.labDescription)
            assertEquals(labDuration, labById.labDuration.minutes)
            assertEquals(randomLabQueueLimit, labById.labQueueLimit)
            assertEquals(labCreatedAt, labById.createdAt)
            assertEquals(userId, labById.ownerId)
            assertTrue(labById.id >= 0)

            // then: retrieving a laboratory by name
            val labByName = laboratoryRepo.getLaboratoryByName(labName)
            assertNotNull(labByName) { "No laboratory retrieved from database" }
            assertEquals(labName, labByName!!.labName)
            assertEquals(labDescription, labByName.labDescription)
            assertEquals(labDuration, labByName.labDuration.minutes)
            assertEquals(randomLabQueueLimit, labByName.labQueueLimit)
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
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            // when: updating the laboratory name
            val newLabName = repoUtils.newTestLabName()
            assertTrue(laboratoryRepo.updateLaboratory(labId, newLabName))

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
    fun `update laboratory description`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            // when: updating the laboratory description
            val newLabDescription = repoUtils.newTestLabDescription()
            assertTrue(laboratoryRepo.updateLaboratory(labId, labDescription = newLabDescription))

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabDescription, updatedLabById!!.labDescription)
        }
    }

    @Test
    fun `update laboratory name and description`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            // when: updating the laboratory name and description
            val newLabName = repoUtils.newTestLabName()
            val newLabDescription = repoUtils.newTestLabDescription()
            assertTrue(laboratoryRepo.updateLaboratory(labId, newLabName, newLabDescription))

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabName, updatedLabById!!.labName)
            assertEquals(newLabDescription, updatedLabById.labDescription)
        }
    }

    @Test
    fun `add group to laboratory and remove it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and group repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            // when: storing a group
            val groupId = repoUtils.createTestGroup(userId, handle)

            // when: adding a group to the laboratory
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

    @Test
    fun `add hardware to laboratory and remove it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and hardware repo
            val laboratoryRepo = JdbiLaboratoryRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a hardware
            val hwId = repoUtils.createTestHardware(handle)

            // when: storing a laboratory
            val labName = repoUtils.newTestLabName()
            val labDescription = repoUtils.newTestLabDescription()
            val labDuration = repoUtils.newTestLabDuration()
            val labCreatedAt = clock.now()
            val randomLabQueueLimit = repoUtils.randomLabQueueLimit()
            val labId = laboratoryRepo.createLaboratory(
                labName,
                labDescription,
                labDuration,
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )

            // when: adding a hardware to the laboratory
            assertTrue(laboratoryRepo.addHardwareToLaboratory(labId, hwId))

            // then: retrieving the laboratory hardware
            val hardwares = laboratoryRepo.getLaboratoryHardware(labId)
            assertNotNull(hardwares) { "No hardwares retrieved from database" }
            assertTrue(hardwares.contains(hwId)) { "Hardware not found in laboratory hardwares" }
            assertTrue(hardwares.size == 1) { "Unexpected number of hardwares in laboratory" }

            // when: removing the hardware from the laboratory
            assertTrue(laboratoryRepo.removeHardwareLaboratory(labId, hwId))

            // then: retrieving the laboratory hardwares should be empty
            val hardwaresAfterRemoval = laboratoryRepo.getLaboratoryHardware(labId)
            assertNotNull(hardwaresAfterRemoval) { "No hardwares retrieved from database" }
            assertTrue(hardwaresAfterRemoval.isEmpty()) { "Hardwares should be empty after removal" }
        }
    }

    companion object {
        private val repoUtils = RepoUtils()
    }
}