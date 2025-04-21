package isel.rl.core.repository.jdbi

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.ValidatedLaboratory
import isel.rl.core.repository.LaboratoriesRepository
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import kotlin.time.DurationUnit

data class JdbiLaboratoriesRepository(
    val handle: Handle,
) : LaboratoriesRepository {
    override fun createLaboratory(
        validatedLaboratory: ValidatedLaboratory
    ): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.laboratory (lab_name, lab_description, lab_duration, lab_queue_limit, created_at, owner_id)
            VALUES (:lab_name, :lab_description, :lab_duration, :lab_queue_limit, :created_at, :owner_id)
        """,
        )
            .bind("lab_name", validatedLaboratory.labName.labNameInfo)
            .bind("lab_description", validatedLaboratory.labDescription.labDescriptionInfo)
            .bind("lab_duration", validatedLaboratory.labDuration.labDurationInfo.toInt(DurationUnit.MINUTES))
            .bind("lab_queue_limit", validatedLaboratory.labQueueLimit.labQueueLimitInfo)
            .bind("created_at", validatedLaboratory.createdAt.toJavaInstant())
            .bind("owner_id", validatedLaboratory.ownerId)
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

    override fun updateLaboratory(
        labId: Int,
        labName: LabName?,
        labDescription: LabDescription?,
    ): Boolean {
        val updateQuery = StringBuilder("UPDATE rl.laboratory SET ")
        val params = mutableMapOf<String, Any>()

        if (labName != null) {
            updateQuery.append("lab_name = :lab_name, ")
            params["lab_name"] = labName.labNameInfo
        }

        if (labDescription != null) {
            updateQuery.append("lab_description = :lab_description, ")
            params["lab_description"] = labDescription.labDescriptionInfo
        }

        updateQuery.delete(updateQuery.length - 2, updateQuery.length)
        updateQuery.append(" WHERE id = :id")
        params["id"] = labId

        return handle.createUpdate(updateQuery.toString())
            .bindMap(params)
            .execute() == 1
    }

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
