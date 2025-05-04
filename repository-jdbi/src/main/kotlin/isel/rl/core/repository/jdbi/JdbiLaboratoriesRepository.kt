package isel.rl.core.repository.jdbi

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.domain.ValidatedCreateLaboratory
import isel.rl.core.domain.laboratory.domain.ValidatedUpdateLaboratory
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.repository.LaboratoriesRepository
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import kotlin.time.DurationUnit

data class JdbiLaboratoriesRepository(
    val handle: Handle,
) : LaboratoriesRepository {
    override fun createLaboratory(validatedCreateLaboratory: ValidatedCreateLaboratory): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.laboratory (lab_name, lab_description, lab_duration, lab_queue_limit, created_at, owner_id)
            VALUES (:lab_name, :lab_description, :lab_duration, :lab_queue_limit, :created_at, :owner_id)
        """,
        )
            .bind("lab_name", validatedCreateLaboratory.labName.labNameInfo)
            .bind("lab_description", validatedCreateLaboratory.labDescription.labDescriptionInfo)
            .bind("lab_duration", validatedCreateLaboratory.labDuration.labDurationInfo.toInt(DurationUnit.MINUTES))
            .bind("lab_queue_limit", validatedCreateLaboratory.labQueueLimit.labQueueLimitInfo)
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
            WHERE lab_name = :lab_name
        """,
        )
            .bind("lab_name", labName.labNameInfo)
            .mapTo<Laboratory>()
            .singleOrNull()

    override fun getLaboratoriesByUserId(userId: Int, limitAndSkip: LimitAndSkip): List<Laboratory> =
        handle.createQuery(
            """
            SELECT l.* FROM rl.laboratory AS l
            JOIN rl.group_laboratory AS gl ON l.id = gl.lab_id
            JOIN rl.user_group AS gu ON gl.group_id = gu.group_id
            WHERE gu.user_id = :user_id
            LIMIT :limit OFFSET :skip
        """
        )
            .bind("user_id", userId)
            .bind("limit", limitAndSkip.limit)
            .bind("skip", limitAndSkip.skip)
            .mapTo<Laboratory>()
            .list()


    override fun updateLaboratory(validatedUpdateLaboratory: ValidatedUpdateLaboratory): Boolean {
        val updateQuery =
            StringBuilder(
                """
            UPDATE rl.laboratory 
            SET 
        """,
            )
        val params = mutableMapOf<String, Any?>()

        validatedUpdateLaboratory.labName?.let {
            updateQuery.append("lab_name = :lab_name, ")
            params["lab_name"] = it.labNameInfo
        }
        validatedUpdateLaboratory.labDescription?.let {
            updateQuery.append("lab_description = :lab_description, ")
            params["lab_description"] = it.labDescriptionInfo
        }
        validatedUpdateLaboratory.labDuration?.let {
            updateQuery.append("lab_duration = :lab_duration, ")
            params["lab_duration"] = it.labDurationInfo.toInt(DurationUnit.MINUTES)
        }
        validatedUpdateLaboratory.labQueueLimit?.let {
            updateQuery.append("lab_queue_limit = :lab_queue_limit, ")
            params["lab_queue_limit"] = it.labQueueLimitInfo
        }

        // Remove the last comma and space
        if (params.isNotEmpty()) {
            updateQuery.setLength(updateQuery.length - 2)
        }

        updateQuery.append(" WHERE id = :id")
        params["id"] = validatedUpdateLaboratory.labId

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
    ): Boolean {
        // Check first if the user is the owner
        return if (getLaboratoryOwnerId(labId) == userId) {
            true
        } else { // Check if the user is part of any group in the laboratory
            handle.createQuery(
                """
            SELECT EXISTS(
                SELECT 1 FROM rl.group_laboratory AS gl
                JOIN rl.group_user AS gu ON gl.group_id = gu.group_id
                WHERE gl.lab_id = :lab_id AND gu.user_id = :user_id
            )
        """,
            )
                .bind("lab_id", labId)
                .bind("user_id", userId)
                .mapTo<Boolean>()
                .one()
        }
    }

    override fun getLaboratoryGroups(labId: Int): List<Int> =
        handle.createQuery(
            """
            SELECT group_id FROM rl.group_laboratory 
            WHERE lab_id = :lab_id
        """,
        )
            .bind("lab_id", labId)
            .mapTo<Int>()
            .list()

    override fun addHardwareToLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean =
        handle.createUpdate(
            """
            INSERT INTO rl.hardware_laboratory (lab_id, hw_id)
            VALUES (:lab_id, :hw_id)
        """,
        )
            .bind("hw_id", hwId)
            .bind("lab_id", labId)
            .execute() == 1

    override fun getLaboratoryHardware(labId: Int): List<Int> =
        handle.createQuery(
            """
            SELECT hw_id FROM rl.hardware_laboratory 
            WHERE lab_id = :lab_id
        """,
        )
            .bind("lab_id", labId)
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
}
