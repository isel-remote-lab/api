package isel.rl.core.repository

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.ValidatedLaboratory

interface LaboratoriesRepository {
    fun createLaboratory(
        validatedLaboratory: ValidatedLaboratory
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
