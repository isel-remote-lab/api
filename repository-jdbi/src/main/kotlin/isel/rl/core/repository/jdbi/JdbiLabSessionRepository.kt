package isel.rl.core.repository.jdbi

import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.domain.laboratory.session.LabSessionState
import isel.rl.core.repository.LabSessionRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

data class JdbiLabSessionRepository(
    val handle: Handle,
) : LabSessionRepository {
    override fun createLabSession(
        labId: Int,
        ownerId: Int,
        startTime: Instant,
        endTime: Instant,
        state: LabSessionState,
    ): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.lab_session (lab_id, owner_id, start_time, end_time, state)
            VALUES (:lab_id, :owner_id, :start_time, :end_time, :state)
            """,
        )
            .bind("lab_id", labId)
            .bind("owner_id", ownerId)
            .bind("start_time", startTime.toJavaInstant())
            .bind("end_time", endTime.toJavaInstant())
            .bind("state", state.char)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getLabSessionById(labSessionId: Int): LabSession? =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE id = :id
            """,
        )
            .bind("id", labSessionId)
            .mapTo<LabSession>()
            .singleOrNull()

    override fun getLabSessionsByLabId(labId: Int): List<LabSession> =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE lab_id = :lab_id
            """,
        )
            .bind("lab_id", labId)
            .mapTo<LabSession>()
            .list()

    override fun getLabSessionsByUserId(userId: Int): List<LabSession> =
        handle.createQuery(
            """
            SELECT * FROM rl.lab_session 
            WHERE owner_id = :owner_id
            """,
        )
            .bind("owner_id", userId)
            .mapTo<LabSession>()
            .list()

    override fun updateLabSession(
        labSessionId: Int,
        startTime: Instant?,
        endTime: Instant?,
        state: LabSessionState?,
    ): Boolean {
        val updateQuery = StringBuilder("UPDATE rl.lab_session SET ")
        val params = mutableMapOf<String, Any?>()

        startTime?.let {
            updateQuery.append("start_time = :start_time, ")
            params["start_time"] = it.toJavaInstant()
        }
        endTime?.let {
            updateQuery.append("end_time = :end_time, ")
            params["end_time"] = it.toJavaInstant()
        }
        state?.let {
            updateQuery.append("state = :state, ")
            params["state"] = it.char
        }

        // Remove the last comma and space
        if (params.isNotEmpty()) {
            updateQuery.setLength(updateQuery.length - 2)
        }

        updateQuery.append(" WHERE id = :id")
        params["id"] = labSessionId

        return handle.createUpdate(updateQuery.toString())
            .bindMap(params)
            .execute() == 1
    }

    override fun removeLabSessionById(labSessionId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.lab_session 
            WHERE id = :id
            """,
        )
            .bind("id", labSessionId)
            .execute() == 1
}
