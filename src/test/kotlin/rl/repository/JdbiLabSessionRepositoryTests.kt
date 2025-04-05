package rl.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import rl.TestClock
import rl.repositoryJdbi.JdbiLabSessionRepository
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JdbiLabSessionRepositoryTests {
    @Test
    fun `store lab session, retrieve it and delete it`() {
        repoUtils.runWithHandle { handle ->
            // given: a lab session and user repo
            val labSessionRepo = JdbiLabSessionRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: storing a lab session
            val startTime = clock.now()
            val endTime = startTime.plus(repoUtils.newTestLabDuration())
            val labSessionState = repoUtils.randomLabSessionState()
            val labSessionId = labSessionRepo.createLabSession(labId, userId, startTime, endTime, labSessionState)

            // then: retrieve lab session by Id
            val labSessionById = labSessionRepo.getLabSessionById(labSessionId)
            assertNotNull(labSessionById) { "No lab session retrieved from database" }
            assertEquals(labId, labSessionById!!.labId)
            assertEquals(userId, labSessionById.ownerId)
            assertEquals(startTime, labSessionById.startTime)
            assertEquals(endTime, labSessionById.endTime)
            assertEquals(labSessionState, labSessionById.state)
            assertTrue(labSessionById.id >= 0)

            // then: retrieving the lab session by labId
            val labSessionByLabId = labSessionRepo.getLabSessionsByLabId(labId)
            assertNotNull(labSessionByLabId) { "No lab session retrieved from database" }
            assertTrue(labSessionByLabId.size == 1)
            assertEquals(labId, labSessionByLabId[0].labId)
            assertEquals(userId, labSessionByLabId[0].ownerId)
            assertEquals(startTime, labSessionByLabId[0].startTime)
            assertEquals(endTime, labSessionByLabId[0].endTime)
            assertEquals(labSessionState, labSessionByLabId[0].state)
            assertTrue(labSessionByLabId[0].id >= 0)

            // then: retrieving the lab session by userId
            val labSessionByUserId = labSessionRepo.getLabSessionsByUserId(userId)
            assertNotNull(labSessionByUserId) { "No lab session retrieved from database" }
            assertTrue(labSessionByUserId.size == 1)
            assertEquals(labId, labSessionByUserId[0].labId)
            assertEquals(userId, labSessionByUserId[0].ownerId)
            assertEquals(startTime, labSessionByUserId[0].startTime)
            assertEquals(endTime, labSessionByUserId[0].endTime)
            assertEquals(labSessionState, labSessionByUserId[0].state)
            assertTrue(labSessionByUserId[0].id >= 0)

            // when: deleting the lab session
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId))
            val deletedLabSession = labSessionRepo.getLabSessionById(labSessionId)
            assertNull(deletedLabSession, "Lab session should be deleted")
        }
    }

    @Test
    fun `update lab session`() {
        repoUtils.runWithHandle { handle ->
            // given: a lab session and user repo
            val labSessionRepo = JdbiLabSessionRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: storing a lab session
            val startTime = clock.now()
            val endTime = startTime.plus(repoUtils.newTestLabDuration())
            val labSessionState = repoUtils.randomLabSessionState()
            val labSessionId = labSessionRepo.createLabSession(labId, userId, startTime, endTime, labSessionState)

            // when: updating the lab session
            val newStartTime = startTime.plus(repoUtils.newTestLabDuration())
            val newEndTime = newStartTime.plus(repoUtils.newTestLabDuration())
            val newLabSessionState = repoUtils.randomLabSessionState()
            labSessionRepo.updateLabSession(labSessionId, newStartTime, newEndTime, newLabSessionState)

            // then: retrieve updated lab session by Id
            val updatedLabSessionById = labSessionRepo.getLabSessionById(labSessionId)
            assertNotNull(updatedLabSessionById) { "No updated lab session retrieved from database" }
            assertEquals(labId, updatedLabSessionById!!.labId)
            assertEquals(userId, updatedLabSessionById.ownerId)
            assertEquals(newStartTime, updatedLabSessionById.startTime)
            assertEquals(newEndTime, updatedLabSessionById.endTime)
            assertEquals(labSessionState, updatedLabSessionById.state)

            // then: delete the lab session
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId))
            val deletedLabSession = labSessionRepo.getLabSessionById(labSessionId)
            assertNull(deletedLabSession)
        }
    }

    @Test
    fun `update lab session state`() {
        repoUtils.runWithHandle { handle ->
            // given: a lab session and user repo
            val labSessionRepo = JdbiLabSessionRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: storing a lab session
            val startTime = clock.now()
            val endTime = startTime.plus(repoUtils.newTestLabDuration())
            val labSessionState = repoUtils.randomLabSessionState()
            val labSessionId = labSessionRepo.createLabSession(labId, userId, startTime, endTime, labSessionState)

            // when: updating the lab session state
            val newLabSessionState = repoUtils.randomLabSessionState()
            labSessionRepo.updateLabSession(labSessionId, null, null, newLabSessionState)

            // then: retrieve updated lab session by Id
            val updatedLabSessionById = labSessionRepo.getLabSessionById(labSessionId)
            assertNotNull(updatedLabSessionById) { "No updated lab session retrieved from database" }
            assertEquals(labId, updatedLabSessionById!!.labId)
            assertEquals(userId, updatedLabSessionById.ownerId)
            assertEquals(startTime, updatedLabSessionById.startTime)
            assertEquals(endTime, updatedLabSessionById.endTime)
            assertEquals(newLabSessionState, updatedLabSessionById.state)

            // then: delete the lab session
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId))
        }
    }

    @Test
    fun `store two lab session and retrieve them`() {
        repoUtils.runWithHandle { handle ->
            // given: a lab session and user repo
            val labSessionRepo = JdbiLabSessionRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: storing two lab sessions
            val startTime1 = clock.now()
            val endTime1 = startTime1.plus(repoUtils.newTestLabDuration())
            val labSessionState1 = repoUtils.randomLabSessionState()
            val labSessionId1 = labSessionRepo.createLabSession(labId, userId, startTime1, endTime1, labSessionState1)

            val startTime2 = startTime1.plus(repoUtils.newTestLabDuration())
            val endTime2 = startTime2.plus(repoUtils.newTestLabDuration())
            val labSessionState2 = repoUtils.randomLabSessionState()
            val labSessionId2 = labSessionRepo.createLabSession(labId, userId, startTime2, endTime2, labSessionState2)

            // then: retrieve both lab sessions by lab Id
            val labSessionsByLabId = labSessionRepo.getLabSessionsByLabId(labId)
            assertNotNull(labSessionsByLabId) { "No lab sessions retrieved from database" }
            assertTrue(labSessionsByLabId.size == 2)
            assertEquals(labId, labSessionsByLabId[0].labId)
            assertEquals(userId, labSessionsByLabId[0].ownerId)
            assertEquals(startTime1, labSessionsByLabId[0].startTime)
            assertEquals(endTime1, labSessionsByLabId[0].endTime)
            assertEquals(labSessionState1, labSessionsByLabId[0].state)
            assertTrue(labSessionsByLabId[0].id >= 0)

            // check the second lab session
            assertEquals(labId, labSessionsByLabId[1].labId)
            assertEquals(userId, labSessionsByLabId[1].ownerId)
            assertEquals(startTime2, labSessionsByLabId[1].startTime)
            assertEquals(endTime2, labSessionsByLabId[1].endTime)
            assertTrue(labSessionsByLabId[1].id >= 0)

            // then: retrieve both lab sessions by user Id
            val labSessionsByUserId = labSessionRepo.getLabSessionsByUserId(userId)
            assertNotNull(labSessionsByUserId) { "No lab sessions retrieved from database" }
            assertTrue(labSessionsByUserId.size == 2)
            assertEquals(labId, labSessionsByUserId[0].labId)
            assertEquals(userId, labSessionsByUserId[0].ownerId)
            assertEquals(startTime1, labSessionsByUserId[0].startTime)
            assertEquals(endTime1, labSessionsByUserId[0].endTime)
            assertEquals(labSessionState1, labSessionsByUserId[0].state)
            assertTrue(labSessionsByUserId[0].id >= 0)

            // check the second lab session
            assertEquals(labId, labSessionsByUserId[1].labId)
            assertEquals(userId, labSessionsByUserId[1].ownerId)
            assertEquals(startTime2, labSessionsByUserId[1].startTime)
            assertEquals(endTime2, labSessionsByUserId[1].endTime)
            assertEquals(labSessionState2, labSessionsByUserId[1].state)
            assertTrue(labSessionsByUserId[1].id >= 0)

            // when: deleting the lab sessions
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId1))
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId2))
        }
    }

    companion object{
        private val repoUtils = RepoUtils()
    }
}