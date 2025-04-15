package isel.rl.core.repository

import isel.rl.core.domain.laboratory.LabDescription
import isel.rl.core.domain.laboratory.LabName
import isel.rl.core.domain.laboratory.Laboratory
import kotlinx.datetime.Instant
import kotlin.time.Duration

interface LaboratoryRepository {
    fun createLaboratory(
        labName: LabName,
        labDescription: LabDescription,
        labDuration: Duration,
        labQueueLimit: Int,
        createdAt: Instant,
        ownerId: Int,
    ): Int

    fun getLaboratoryById(labId: Int): Laboratory?

    fun getLaboratoryByName(labName: LabName): Laboratory?

    fun updateLaboratory(
        labId: Int,
        labName: LabName? = null,
        labDescription: LabDescription? = null,
    ): Boolean

    fun deleteLaboratory(labId: Int): Boolean

    fun addGroupToLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean

    fun removeGroupFromLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean

    fun getLaboratoryGroups(labId: Int): List<Int>

    fun addHardwareToLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean

    fun getLaboratoryHardware(labId: Int): List<Int>

    fun removeHardwareLaboratory(
        labId: Int,
        hwId: Int,
    ): Boolean
}
