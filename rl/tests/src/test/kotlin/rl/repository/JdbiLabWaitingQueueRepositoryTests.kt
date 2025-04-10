package rl.repository

import org.junit.jupiter.api.Test
import rl.jdbi.JdbiLabWaitingQueueRepository

class JdbiLabWaitingQueueRepositoryTests {
    @Test
    fun `insert user into queue, verify position, and remove it`() {
        repoUtils.runWithHandle { handle ->
            val labWaitingQueueRepo = JdbiLabWaitingQueueRepository(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: inserting the user into the queue
            labWaitingQueueRepo.addUserToLabQueue(labId, userId)

            // then: verify the user's position in the queue
            val position = labWaitingQueueRepo.getUserQueuePosition(labId, userId)
            assert(position == 1) { "Expected position to be 1 but was $position" }

            // when: removing the user from the queue
            labWaitingQueueRepo.removeUserLabQueue(labId, userId)

            // then: verify that the queue is empty
            assert(labWaitingQueueRepo.isLabQueueEmpty(labId)) { "Expected queue to be empty but it wasn't" }
        }
    }

    @Test
    fun `pop user from queue`() {
        repoUtils.runWithHandle { handle ->
            val labWaitingQueueRepo = JdbiLabWaitingQueueRepository(handle)

            // when: storing a user
            val userId = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: inserting the user into the queue
            labWaitingQueueRepo.addUserToLabQueue(labId, userId)

            // when: popping a user from the queue
            val poppedUserId = labWaitingQueueRepo.popLabQueue(labId)

            // then: verify that the popped user is the same as the one we inserted
            assert(poppedUserId == userId) { "Expected popped user ID to be $userId but was $poppedUserId" }
        }
    }

    @Test
    fun `pop a user from a queue with more than one users`() {
        repoUtils.runWithHandle { handle ->
            val labWaitingQueueRepo = JdbiLabWaitingQueueRepository(handle)

            // when: storing a user
            val userId1 = repoUtils.createTestUser(handle)

            // when: storing another user
            val userId2 = repoUtils.createTestUser(handle)

            // when: storing a laboratory
            val labId = repoUtils.createTestLab(handle)

            // when: inserting both users into the queue
            labWaitingQueueRepo.addUserToLabQueue(labId, userId1)
            labWaitingQueueRepo.addUserToLabQueue(labId, userId2)

            // when: popping a user from the queue
            val poppedUserId = labWaitingQueueRepo.popLabQueue(labId)

            // then: verify that the popped user is the first one we inserted
            assert(poppedUserId == userId1) { "Expected popped user ID to be $userId1 but was $poppedUserId" }

            // when: checking the queue size
            val queueSize = labWaitingQueueRepo.getQueueSize(labId)

            // then: verify that the queue size is 1
            assert(queueSize == 1) { "Expected queue size to be 1 but was $queueSize" }

            // when: checking the position of the second user
            val position = labWaitingQueueRepo.getUserQueuePosition(labId, userId2)

            // then: verify that the position of the second user is 1
            assert(position == 1) { "Expected position to be 1 but was $position" }

            // when: removing the second user from the queue
            labWaitingQueueRepo.removeUserLabQueue(labId, userId2)

            // then: verify that the queue is empty
            assert(labWaitingQueueRepo.isLabQueueEmpty(labId)) { "Expected queue to be empty but it wasn't" }

            // when: checking the queue size
            val queueSizeAfterRemoval = labWaitingQueueRepo.getQueueSize(labId)

            // then: verify that the queue size is 0
            assert(queueSizeAfterRemoval == 0) { "Expected queue size to be 0 but was $queueSizeAfterRemoval" }
        }
    }

    companion object {
        private val repoUtils = RepoUtils()
    }
}