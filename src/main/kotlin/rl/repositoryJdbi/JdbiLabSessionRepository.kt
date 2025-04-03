package rl.repositoryJdbi

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import rl.domain.laboratory.LabSession
import rl.repository.LabSessionRepository

data class JdbiLabSessionRepository(
    val handle: Handle
) : LabSessionRepository {
    override fun createLabSession(labId: Int, ownerId: Int, startTime: Instant, endTime: Instant): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.lab_session (lab_id, owner_id, start_time, end_time)
            VALUES (:lab_id, :owner_id, :start_time, :end_time)
            """
        )
            .bind("lab_id", labId)
            .bind("owner_id", ownerId)
            .bind("start_time", startTime.toJavaInstant())
            .bind("end_time", endTime.toJavaInstant())
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getLabSessionById(labSessionId: Int): LabSession? =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE id = :id
            """
        )
            .bind("id", labSessionId)
            .mapTo<LabSession>()
            .singleOrNull()

    override fun getLabSessionsByLabId(labId: Int): List<LabSession> =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE lab_id = :lab_id
            """
        )
            .bind("lab_id", labId)
            .mapTo<LabSession>()
            .list()

    override fun getLabSessionsByUserId(userId: Int): List<LabSession> =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE owner_id = :owner_id
            """
        )
            .bind("owner_id", userId)
            .mapTo<LabSession>()
            .list()

    override fun updateLabSession(labSessionId: Int, startTime: Instant, endTime: Instant): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.lab_session 
            SET start_time = :start_time, end_time = :end_time
            WHERE id = :id
            """
        )
            .bind("start_time", startTime.toJavaInstant())
            .bind("end_time", endTime.toJavaInstant())
            .bind("id", labSessionId)
            .execute() == 1

    override fun removeLabSessionById(labSessionId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.lab_session 
            WHERE id = :id
            """
        )
            .bind("id", labSessionId)
            .execute() == 1
}
