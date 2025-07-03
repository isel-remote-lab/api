package isel.rl.core.repository.jdbi

import isel.rl.core.repository.LabWaitingQueueRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

data class JdbiLabWaitingQueueRepository(
    val handle: Handle,
) : LabWaitingQueueRepository {
    override fun addUserToLabQueue(
        labId: Int,
        userId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.lab_waiting_queue (user_id, lab_id)
            VALUES (:user_id, :lab_id)
        """,
        )
            .bind("user_id", userId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun removeUserLabQueue(
        labId: Int,
        userId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.lab_waiting_queue 
            WHERE lab_id = :lab_id AND user_id = :user_id
        """,
        )
            .bind("user_id", userId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun isLabQueueEmpty(labId: Int): Boolean =
        handle.createQuery(
            """
            SELECT COUNT(*) = 0
            FROM rl.lab_waiting_queue
            WHERE lab_id = :lab_id
        """,
        )
            .bind("lab_id", labId)
            .mapTo<Boolean>()
            .one()

    override fun getQueueSize(labId: Int): Int =
        handle.createQuery(
            """
            SELECT COUNT(*)
            FROM rl.lab_waiting_queue
            WHERE lab_id = :lab_id
        """,
        )
            .bind("lab_id", labId)
            .mapTo<Int>()
            .one()

    override fun getUserQueuePosition(
        labId: Int,
        userId: Int,
    ): Int =
        handle.createQuery(
            """
            SELECT COUNT(*) + 1
            FROM rl.lab_waiting_queue
            WHERE lab_id = :lab_id AND id < (
                SELECT id
                FROM rl.lab_waiting_queue
                WHERE lab_id = :lab_id AND user_id = :user_id
            )
        """,
        )
            .bind("lab_id", labId)
            .bind("user_id", userId)
            .mapTo<Int>()
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
            """,
        )
            .bind("lab_id", labId)
            .executeAndReturnGeneratedKeys("user_id")
            .mapTo<Int>()
            .one()

    override fun getUsersInQueue(labId: Int): List<Int> =
        handle.createQuery(
            """
            SELECT user_id
            FROM rl.lab_waiting_queue
            WHERE lab_id = :lab_id
            ORDER BY id
        """,
        )
            .bind("lab_id", labId)
            .mapTo<Int>()
            .list()
}
