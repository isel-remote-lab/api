package isel.rl.core.repository

import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.repository.jdbi.JdbiLaboratoriesRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.DurationUnit

class JdbiLaboratoriesRepositoryTests {
    @Test
    fun `store laboratory and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // then: retrieve laboratory by Id and verify it
            val labById = laboratoryRepo.getLaboratoryById(labId)
            initialLaboratory.assertLabWith(labById)

            // then: retrieving a laboratory by name and verify it
            val labByName = laboratoryRepo.getLaboratoryByName(initialLaboratory.labName)
            initialLaboratory.assertLabWith(labByName)
        }
    }

    @Test
    fun `update laboratory name and delete it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: updating the laboratory name
            val newLabName = repoUtils.newTestLabName()
            assertTrue(
                laboratoryRepo.updateLaboratory(
                    repoUtils.laboratoriesDomain.validateUpdateLaboratory(
                        labId,
                        newLabName.labNameInfo,
                    ),
                ),
            )

            // then: retrieving the updated laboratory by name
            val updatedLabByName = laboratoryRepo.getLaboratoryByName(newLabName)
            assertNotNull(updatedLabByName) { "No updated laboratory retrieved from database" }
            assertEquals(newLabName, updatedLabByName.labName, "Lab names do not match")

            // when: deleting the laboratory
            assertTrue(laboratoryRepo.deleteLaboratory(labId), "Laboratory not deleted")

            // then: retrieving the deleted laboratory by Id should return null
            assertEquals(null, laboratoryRepo.getLaboratoryById(labId), "Deleted laboratory not found")
        }
    }

    @Test
    fun `update laboratory description`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: updating the laboratory description
            val newLabDescription = repoUtils.newTestLabDescription()
            assertTrue(
                laboratoryRepo.updateLaboratory(
                    repoUtils.laboratoriesDomain.validateUpdateLaboratory(
                        labId,
                        labDescription = newLabDescription.labDescriptionInfo,
                    ),
                ),
            )

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabDescription, updatedLabById.labDescription, "Lab descriptions do not match")
        }
    }

    @Test
    fun `update laboratory duration`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: updating the laboratory duration
            val newLabDuration = repoUtils.newTestLabDuration()
            assertTrue(
                laboratoryRepo.updateLaboratory(
                    repoUtils.laboratoriesDomain.validateUpdateLaboratory(
                        labId,
                        labDuration = newLabDuration.labDurationInfo.toInt(DurationUnit.MINUTES),
                    ),
                ),
            )

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabDuration, updatedLabById.labDuration, "Lab durations do not match")
        }
    }

    @Test
    fun `update laboratory queue limit`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: updating the laboratory queue limit
            val newLabQueueLimit = repoUtils.randomLabQueueLimit()
            assertTrue(
                laboratoryRepo.updateLaboratory(
                    repoUtils.laboratoriesDomain.validateUpdateLaboratory(
                        labId,
                        labQueueLimit = newLabQueueLimit.labQueueLimitInfo,
                    ),
                ),
            )

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabQueueLimit, updatedLabById.labQueueLimit, "Lab queue limits do not match")
        }
    }

    @Test
    fun `update laboratory name and description`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory and user repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: updating the laboratory name and description
            val newLabName = repoUtils.newTestLabName()
            val newLabDescription = repoUtils.newTestLabDescription()
            assertTrue(
                laboratoryRepo.updateLaboratory(
                    repoUtils.laboratoriesDomain.validateUpdateLaboratory(
                        labId,
                        newLabName.labNameInfo,
                        newLabDescription.labDescriptionInfo,
                    ),
                ),
            )

            // then: retrieving the updated laboratory by Id
            val updatedLabById = laboratoryRepo.getLaboratoryById(labId)
            assertNotNull(updatedLabById) { "No updated laboratory retrieved from database" }
            assertEquals(newLabName, updatedLabById.labName, "Lab names do not match")
            assertEquals(newLabDescription, updatedLabById.labDescription, "Lab descriptions do not match")
        }
    }

    @Test
    fun `add group to laboratory and remove it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and group repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: storing a group
            val groupId = repoUtils.createTestGroup(userId, handle)

            // when: adding a group to the laboratory
            assertTrue(laboratoryRepo.addGroupToLaboratory(labId, groupId))

            // then: retrieving the laboratory groups
            val groups = laboratoryRepo.getLaboratoryGroups(labId)
            assertNotNull(groups) { "No groups retrieved from database" }
            assertTrue(groups.contains(groupId), "Group not found in laboratory groups")
            assertTrue(groups.size == 1, "Unexpected number of groups in laboratory")

            // when: removing the group from the laboratory
            assertTrue(laboratoryRepo.removeGroupFromLaboratory(labId, groupId))

            // then: retrieving the laboratory groups should be empty
            val groupsAfterRemoval = laboratoryRepo.getLaboratoryGroups(labId)
            assertNotNull(groupsAfterRemoval) { "No groups retrieved from database" }
            assertTrue(groupsAfterRemoval.isEmpty(), "Groups should be empty after removal")
        }
    }

    @Test
    fun `check if user belongs to laboratory (success)`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and group repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: storing a group
            val groupId = repoUtils.createTestGroup(userId, handle)

            // when: adding a group to the laboratory
            assertTrue(laboratoryRepo.addGroupToLaboratory(labId, groupId))

            // when: checking if the user belongs to the laboratory
            assertTrue(laboratoryRepo.checkIfUserBelongsToLaboratory(labId, userId))
        }
    }

    @Test
    fun `check if user belongs to laboratory (failure)`()  {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and group repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: checking if the user belongs to the laboratory
            assertFalse(!laboratoryRepo.checkIfUserBelongsToLaboratory(labId, userId))
        }
    }

    @Test
    fun `add hardware to laboratory and remove it`() {
        repoUtils.runWithHandle { handle ->
            // given: a laboratory, user repo and hardware repo
            val laboratoryRepo = JdbiLaboratoriesRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a hardware
            val hwId = repoUtils.createTestHardware(handle)

            // when: storing a laboratory
            val initialLaboratory = InitialLaboratoryInfo(clock, userId)
            val labId = laboratoryRepo.createLaboratory(initialLaboratory)

            // when: adding a hardware to the laboratory
            assertTrue(laboratoryRepo.addHardwareToLaboratory(labId, hwId))

            // then: retrieving the laboratory hardware
            val hardwares = laboratoryRepo.getLaboratoryHardware(labId)
            assertNotNull(hardwares) { "No hardwares retrieved from database" }
            assertTrue(hardwares.contains(hwId), "Hardware not found in laboratory hardwares")
            assertTrue(hardwares.size == 1, "Unexpected number of hardwares in laboratory")

            // when: removing the hardware from the laboratory
            assertTrue(laboratoryRepo.removeHardwareLaboratory(labId, hwId))

            // then: retrieving the laboratory hardwares should be empty
            val hardwaresAfterRemoval = laboratoryRepo.getLaboratoryHardware(labId)
            assertNotNull(hardwaresAfterRemoval) { "No hardwares retrieved from database" }
            assertTrue(hardwaresAfterRemoval.isEmpty(), "Hardwares should be empty after removal")
        }
    }

    companion object {
        private val repoUtils = RepoUtils()

        private data class InitialLaboratoryInfo(
            val clock: TestClock,
            val userId: Int,
            val labName: LabName = repoUtils.newTestLabName(),
            val labDescription: LabDescription = repoUtils.newTestLabDescription(),
            val labDuration: LabDuration = repoUtils.newTestLabDuration(),
            val labCreatedAt: Instant = clock.now(),
            val labQueueLimit: LabQueueLimit = repoUtils.randomLabQueueLimit(),
        )

        private fun JdbiLaboratoriesRepository.createLaboratory(initialLaboratoryInfo: InitialLaboratoryInfo): Int {
            return createLaboratory(
                repoUtils.laboratoriesDomain.validateCreateLaboratory(
                    initialLaboratoryInfo.labName.labNameInfo,
                    initialLaboratoryInfo.labDescription.labDescriptionInfo,
                    initialLaboratoryInfo.labDuration.labDurationInfo.toInt(DurationUnit.MINUTES),
                    initialLaboratoryInfo.labQueueLimit.labQueueLimitInfo,
                    initialLaboratoryInfo.labCreatedAt,
                    initialLaboratoryInfo.userId,
                ),
            )
        }

        private fun InitialLaboratoryInfo.assertLabWith(lab: Laboratory?) {
            assertNotNull(lab) { "No laboratory retrieved" }
            assertEquals(labName, lab.labName, "Lab names do not match")
            assertEquals(labDescription, lab.labDescription, "Lab descriptions do not match")
            assertEquals(labDuration, lab.labDuration, "Lab durations do not match")
            assertEquals(labQueueLimit, lab.labQueueLimit, "Lab queue limits do not match")
            assertEquals(labCreatedAt, lab.createdAt, "Lab createdAt do not match")
            assertEquals(userId, lab.ownerId, "Lab ownerId do not match")
            assertTrue(lab.id >= 0, "Lab id must be >= 0")
        }
    }
}
