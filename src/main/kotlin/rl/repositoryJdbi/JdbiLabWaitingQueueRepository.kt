package rl.repositoryJdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import rl.repository.LabWaitingQueueRepository

data class JdbiLabWaitingQueueRepository(
    val handle: Handle
): LabWaitingQueueRepository {
    override fun addUserToLabQueue(labId: Int, userId: Int): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.user_waiting_queue (user_id, lab_id)
            VALUES (:user_id, :lab_id)
        """
        )
            .bind("user_id", userId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun removeUserLabQueue(labId: Int, userId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.user_waiting_queue 
            WHERE lab_id = :lab_id AND user_id = :user_id
        """
        )
            .bind("user_id", userId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun isLabQueueEmpty(labId: Int): Boolean =
        handle.createQuery("""
            SELECT COUNT(*) = 0
            FROM rl.lab_waiting_queue
            WHERE lab_id = :lab_id
        """)
            .bind("lab_id", labId)
            .mapTo<Boolean>()
            .one()

    // Returns user id.
    override fun popLabQueue(labId: Int): Int =
        handle.createUpdate(
            """
                WITH popped AS (
                    SELECT id
                    FROM rl.lab_waiting_queue
                    WHERE lab_id = :lab_id
                    ORDER BY id
                    LIMIT 1
                )
                DELETE FROM rl.lab_waiting_queue
                WHERE id IN (SELECT id FROM popped)
                RETURNING user_id
            """
        )
            .bind("lab_id", labId)
            .executeAndReturnGeneratedKeys("user_id")
            .mapTo<Int>()
            .one()


}
