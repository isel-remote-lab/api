package rl.repository

import kotlinx.datetime.Instant
import rl.domain.laboratory.LabName
import rl.domain.laboratory.Laboratory
import kotlin.time.Duration

interface LaboratoryRepository {
    fun createLaboratory(
        labName: LabName,
        labDuration: Duration,
        labQueueLimit: Int,
        createdAt: Instant,
        ownerId: Int
    ): Int

    fun getLaboratoryById(labId: Int): Laboratory?

    fun getLaboratoryByName(labName: LabName): Laboratory?

    fun updateLaboratoryName(
        labId: Int,
        labName: LabName
    ): Boolean

    fun deleteLaboratory(labId: Int): Boolean

    fun addGroupToLaboratory(
        labId: Int,
        groupId: Int
    ): Boolean

    fun removeGroupFromLaboratory(
        labId: Int,
        groupId: Int
    ): Boolean

    fun getLaboratoryGroups(
        labId: Int
    ): List<Int>

    fun addHardwareToLaboratory(
        labId: Int,
        hwId: Int
    ): Boolean

    fun removeHardwareLaboratory(
        labId: Int,
        hwId: Int
    ): Boolean
}