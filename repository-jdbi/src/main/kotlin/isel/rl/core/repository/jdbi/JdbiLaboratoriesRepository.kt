package isel.rl.core.repository.jdbi

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.repository.LaboratoriesRepository
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import kotlin.time.DurationUnit

data class JdbiLaboratoriesRepository(
    val handle: Handle,
) : LaboratoriesRepository {
    override fun createLaboratory(validatedCreateLaboratory: Laboratory): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.laboratory (name, description, duration, queue_limit, created_at, owner_id)
            VALUES (:name, :description, :duration, :queue_limit, :created_at, :owner_id)
        """,
        )
            .bind("name", validatedCreateLaboratory.name.labNameInfo)
            .bind("description", validatedCreateLaboratory.description.labDescriptionInfo)
            .bind("duration", validatedCreateLaboratory.duration.labDurationInfo?.toInt(DurationUnit.MINUTES))
            .bind("queue_limit", validatedCreateLaboratory.queueLimit.labQueueLimitInfo)
            .bind("created_at", validatedCreateLaboratory.createdAt.toJavaInstant())
            .bind("owner_id", validatedCreateLaboratory.ownerId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getLaboratoryById(labId: Int): Laboratory? =
        handle.createQuery(
            """
            SELECT * FROM rl.laboratory 
            WHERE id = :id
        """,
        )
            .bind("id", labId)
            .mapTo<Laboratory>()
            .singleOrNull()

    override fun getLaboratoryByName(labName: LabName): Laboratory? =
        handle.createQuery(
            """
            SELECT * FROM rl.laboratory 
            WHERE name = :name
        """,
        )
            .bind("name", labName.labNameInfo)
            .mapTo<Laboratory>()
            .singleOrNull()

    override fun getLaboratoriesByUserId(
        userId: Int,
        limitAndSkip: LimitAndSkip,
    ): List<Laboratory> =
        handle.createQuery(
            """
            SELECT DISTINCT l.* FROM rl.laboratory AS l
            LEFT JOIN rl.group_laboratory AS gl ON l.id = gl.lab_id
            LEFT JOIN rl.user_group AS gu ON gl.group_id = gu.group_id
            WHERE gu.user_id = :user_id OR l.owner_id = :user_id
            ORDER BY l.id
            LIMIT :limit OFFSET :skip
        """,
        )
            .bind("user_id", userId)
            .bind("limit", limitAndSkip.limit)
            .bind("skip", limitAndSkip.skip)
            .mapTo<Laboratory>()
            .list()

    override fun updateLaboratory(
        labId: Int,
        labName: LabName?,
        labDescription: LabDescription?,
        labDuration: LabDuration?,
        labQueueLimit: LabQueueLimit?,
    ): Boolean {
        val updateQuery =
            StringBuilder(
                """
            UPDATE rl.laboratory 
            SET 
        """,
            )
        val params = mutableMapOf<String, Any?>()

        labName?.let {
            updateQuery.append("name = :name, ")
            params["name"] = it.labNameInfo
        }
        labDescription?.let {
            updateQuery.append("description = :description, ")
            params["description"] = it.labDescriptionInfo
        }
        labDuration?.let {
            updateQuery.append("duration = :duration, ")
            params["duration"] = it.labDurationInfo?.toInt(DurationUnit.MINUTES)
        }
        labQueueLimit?.let {
            updateQuery.append("queue_limit = :queue_limit, ")
            params["queue_limit"] = it.labQueueLimitInfo
        }

        // Remove the last comma and space
        if (params.isNotEmpty()) {
            updateQuery.setLength(updateQuery.length - 2)
        }

        updateQuery.append(" WHERE id = :id")
        params["id"] = labId

        return handle.createUpdate(updateQuery.toString())
            .bindMap(params)
            .execute() == 1
    }

    override fun checkIfLaboratoryExists(labId: Int): Boolean =
        handle.createQuery(
            """
            SELECT EXISTS(SELECT 1 FROM rl.laboratory WHERE id = :id)
        """,
        )
            .bind("id", labId)
            .mapTo<Boolean>()
            .one()

    override fun getLaboratoryOwnerId(labId: Int): Int =
        handle.createQuery(
            """
            SELECT owner_id FROM rl.laboratory 
            WHERE id = :id
        """,
        )
            .bind("id", labId)
            .mapTo<Int>()
            .one()

    override fun deleteLaboratory(labId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.laboratory 
            WHERE id = :id
        """,
        )
            .bind("id", labId)
            .execute() == 1

    override fun addGroupToLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.group_laboratory (group_id, lab_id)
            VALUES (:group_id, :lab_id)
        """,
        )
            .bind("group_id", groupId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun removeGroupFromLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.group_laboratory 
            WHERE lab_id = :lab_id AND group_id = :group_id
        """,
        )
            .bind("group_id", groupId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun checkIfUserBelongsToLaboratory(
        labId: Int,
        userId: Int,
    ): Boolean =
        // Check first if the user is the owner
        if (getLaboratoryOwnerId(labId) == userId) {
            true
        } else { // Check if the user is part of any group in the laboratory
            handle.createQuery(
                """
            SELECT EXISTS(
                SELECT 1 FROM rl.group_laboratory AS gl
                JOIN rl.user_group AS gu ON gl.group_id = gu.group_id
                WHERE gl.lab_id = :lab_id AND gu.user_id = :user_id
            )
        """,
            )
                .bind("lab_id", labId)
                .bind("user_id", userId)
                .mapTo<Boolean>()
                .one()
        }

    override fun getLaboratoryGroups(
        labId: Int,
        limitAndSkip: LimitAndSkip?,
    ): List<Int> {
        val query =
            StringBuilder(
                """
            SELECT group_id FROM rl.group_laboratory 
            WHERE lab_id = :lab_id
        """,
            )

        limitAndSkip?.let {
            query.append(" LIMIT :limit OFFSET :skip")
        }

        val queryHandle =
            handle.createQuery(query.toString())
                .bind("lab_id", labId)

        limitAndSkip?.let {
            queryHandle.bind("limit", it.limit)
                .bind("skip", it.skip)
        }

        return queryHandle.mapTo<Int>()
            .list()
    }

    override fun checkIfGroupBelongsToLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean =
        handle.createQuery(
            """
            SELECT EXISTS(
                SELECT 1 FROM rl.group_laboratory 
                WHERE lab_id = :lab_id AND group_id = :group_id
            )
        """,
        )
            .bind("lab_id", labId)
            .bind("group_id", groupId)
            .mapTo<Boolean>()
            .one()

    override fun addHardwareToLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.hardware_laboratory (hw_id, lab_id)
            VALUES (:hw_id, :lab_id)
        """,
        )
            .bind("hw_id", hwId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun getLaboratoryHardware(
        labId: Int,
        limitAndSkip: LimitAndSkip?,
    ): List<Int> =
        handle.createQuery(
            """
            SELECT hw_id FROM rl.hardware_laboratory 
            WHERE lab_id = :lab_id
            LIMIT :limit OFFSET :skip
        """,
        )
            .bind("lab_id", labId)
            .bind("limit", limitAndSkip?.limit)
            .bind("skip", limitAndSkip?.skip)
            .mapTo<Int>()
            .list()

    override fun removeHardwareLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.hardware_laboratory 
            WHERE hw_id = :hw_id AND lab_id = :lab_id
        """,
        )
            .bind("hw_id", hwId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun checkIfHardwareBelongsToLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean =
        handle.createQuery(
            """
            SELECT EXISTS(
                SELECT 1 FROM rl.hardware_laboratory 
                WHERE lab_id = :lab_id AND hw_id = :hw_id
            )
        """,
        )
            .bind("lab_id", labId)
            .bind("hw_id", hwId)
            .mapTo<Boolean>()
            .one()
}
