package isel.rl.core.repository

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit

interface LaboratoriesRepository {
    fun createLaboratory(validatedCreateLaboratory: Laboratory): Int

    fun getLaboratoryById(labId: Int): Laboratory?

    fun getLaboratoryByName(labName: LabName): Laboratory?

    fun getLaboratoriesByUserId(
        userId: Int,
        limitAndSkip: LimitAndSkip,
    ): List<Laboratory>

    fun updateLaboratory(
        labId: Int,
        labName: LabName? = null,
        labDescription: LabDescription? = null,
        labDuration: LabDuration? = null,
        labQueueLimit: LabQueueLimit? = null,
    ): Boolean

    fun checkIfLaboratoryExists(labId: Int): Boolean

    fun getLaboratoryOwnerId(labId: Int): Int?

    fun deleteLaboratory(labId: Int): Boolean

    fun addGroupToLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean

    fun removeGroupFromLaboratory(
        labId: Int,
        groupId: Int,
    ): Boolean

    fun checkIfUserBelongsToLaboratory(
        labId: Int,
        userId: Int,
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
