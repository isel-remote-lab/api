package isel.rl.core.repository

import isel.rl.core.domain.laboratory.LabSession
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.repository.jdbi.JdbiLabSessionRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
            val initialLabSession = InitialLabSessionInfo(clock, labId, userId)
            val labSessionId =
                labSessionRepo.createLabSession(
                    labId,
                    userId,
                    initialLabSession.startTime,
                    initialLabSession.endTime,
                    initialLabSession.labSessionState,
                )

            // then: retrieve lab session by Id
            val labSessionById = labSessionRepo.getLabSessionById(labSessionId)
            initialLabSession.assertLabSessionWith(labSessionById)

            // then: retrieving the lab session by labId
            val labSessionByLabId = labSessionRepo.getLabSessionsByLabId(labId)
            assertNotNull(labSessionByLabId) { "No lab session retrieved from database" }
            assertTrue(labSessionByLabId.size == 1, "Expected 1 lab session but got ${labSessionByLabId.size}")
            initialLabSession.assertLabSessionWith(labSessionByLabId[0])

            // then: retrieving the lab session by userId
            val labSessionByUserId = labSessionRepo.getLabSessionsByUserId(userId)
            assertNotNull(labSessionByUserId) { "No lab session retrieved from database" }
            assertTrue(labSessionByUserId.size == 1, "Expected 1 lab session but got ${labSessionByUserId.size}")
            initialLabSession.assertLabSessionWith(labSessionByUserId[0])

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
            val initialLabSession = InitialLabSessionInfo(clock, labId, userId)
            val labSessionId =
                labSessionRepo.createLabSession(
                    labId,
                    userId,
                    initialLabSession.startTime,
                    initialLabSession.endTime,
                    initialLabSession.labSessionState,
                )

            // when: updating the lab session
            val newStartTime = initialLabSession.startTime.plus(repoUtils.newTestLabDuration().labDurationInfo)
            val newEndTime = newStartTime.plus(repoUtils.newTestLabDuration().labDurationInfo)
            val newLabSessionState = repoUtils.randomLabSessionState()
            labSessionRepo.updateLabSession(labSessionId, newStartTime, newEndTime, newLabSessionState)

            // then: retrieve updated lab session by Id
            val updatedLabSessionById = labSessionRepo.getLabSessionById(labSessionId)
            assertNotNull(updatedLabSessionById) { "No updated lab session retrieved from database" }
            assertEquals(newStartTime, updatedLabSessionById.startTime, "Start times do not match")
            assertEquals(newEndTime, updatedLabSessionById.endTime, "End times do not match")
            assertEquals(newLabSessionState, updatedLabSessionById.state, "Lab session states do not match")

            // then: delete the lab session
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId))
            val deletedLabSession = labSessionRepo.getLabSessionById(labSessionId)
            assertNull(deletedLabSession, "Lab session should be deleted")
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
            val initialLabSession = InitialLabSessionInfo(clock, labId, userId)
            val labSessionId =
                labSessionRepo.createLabSession(
                    labId,
                    userId,
                    initialLabSession.startTime,
                    initialLabSession.endTime,
                    initialLabSession.labSessionState,
                )

            // when: updating the lab session state
            val newLabSessionState = repoUtils.randomLabSessionState()
            labSessionRepo.updateLabSession(labSessionId, null, null, newLabSessionState)

            // then: retrieve updated lab session by Id
            val updatedLabSessionById = labSessionRepo.getLabSessionById(labSessionId)
            assertNotNull(updatedLabSessionById) { "No updated lab session retrieved from database" }
            assertEquals(initialLabSession.startTime, updatedLabSessionById.startTime, "Start times do not match")
            assertEquals(initialLabSession.endTime, updatedLabSessionById.endTime, "End times do not match")
            assertEquals(newLabSessionState, updatedLabSessionById.state, "Lab session states do not match")

            // then: delete the lab session
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId), "Lab session should be deleted")
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
            val initialLabSession1 = InitialLabSessionInfo(clock, labId, userId)
            val labSessionId1 =
                labSessionRepo.createLabSession(
                    labId,
                    userId,
                    initialLabSession1.startTime,
                    initialLabSession1.endTime,
                    initialLabSession1.labSessionState,
                )
            val initialLabSession2 =
                InitialLabSessionInfo(
                    clock,
                    labId,
                    userId,
                    initialLabSession1.startTime.plus(repoUtils.newTestLabDuration().labDurationInfo),
                    initialLabSession1.endTime.plus(repoUtils.newTestLabDuration().labDurationInfo),
                    repoUtils.randomLabSessionState(),
                )
            val labSessionId2 =
                labSessionRepo.createLabSession(
                    labId,
                    userId,
                    initialLabSession2.startTime,
                    initialLabSession2.endTime,
                    initialLabSession2.labSessionState,
                )

            // then: retrieve both lab sessions by lab Id
            val labSessionsByLabId = labSessionRepo.getLabSessionsByLabId(labId)
            assertNotNull(labSessionsByLabId) { "No lab sessions retrieved from database" }
            assertTrue(labSessionsByLabId.size == 2, "Expected 2 lab sessions but got ${labSessionsByLabId.size}")

            // check both lab sessions
            initialLabSession1.assertLabSessionWith(labSessionsByLabId[0])
            initialLabSession2.assertLabSessionWith(labSessionsByLabId[1])

            // then: retrieve both lab sessions by user Id
            val labSessionsByUserId = labSessionRepo.getLabSessionsByUserId(userId)
            assertNotNull(labSessionsByUserId) { "No lab sessions retrieved from database" }
            assertTrue(labSessionsByUserId.size == 2, "Expected 2 lab sessions but got ${labSessionsByUserId.size}")

            // check both lab sessions
            initialLabSession1.assertLabSessionWith(labSessionsByUserId[0])
            initialLabSession2.assertLabSessionWith(labSessionsByUserId[1])

            // when: deleting the lab sessions
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId1), "Lab session should be deleted")
            assertTrue(labSessionRepo.removeLabSessionById(labSessionId2), "Lab session should be deleted")
        }
    }

    companion object {
        private val repoUtils = RepoUtils()

        private data class InitialLabSessionInfo(
            val clock: TestClock,
            val labId: Int,
            val userId: Int,
            val startTime: Instant = clock.now(),
            val endTime: Instant = startTime.plus(repoUtils.newTestLabDuration().labDurationInfo),
            val labSessionState: LabSessionState = repoUtils.randomLabSessionState(),
        )

        private fun InitialLabSessionInfo.assertLabSessionWith(labSession: LabSession?) {
            assertNotNull(labSession) { "No lab session retrieved" }
            assertEquals(labId, labSession.labId, "Lab IDs do not match")
            assertEquals(userId, labSession.ownerId, "User IDs do not match")
            assertEquals(startTime, labSession.startTime, "Start times do not match")
            assertEquals(endTime, labSession.endTime, "End times do not match")
            assertEquals(labSessionState, labSession.state, "Lab session states do not match")
            assertTrue(labSession.id >= 0, "Lab session ID must be >= 0")
        }
    }
}
