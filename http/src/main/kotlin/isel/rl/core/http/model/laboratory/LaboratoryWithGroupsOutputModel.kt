package isel.rl.core.http.model.laboratory

import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.http.model.group.GroupOutputModel
import kotlin.time.DurationUnit

data class LaboratoryWithGroupsOutputModel(
    val id: Int,
    val labName: String?,
    val labDescription: String?,
    val labDuration: Int?,
    val labQueueLimit: Int?,
    val createdAt: String,
    val ownerId: Int,
    val groups: List<GroupOutputModel>,
) {
    companion object {
        fun mapOf(laboratory: Laboratory) =
            LaboratoryWithGroupsOutputModel(
                id = laboratory.id,
                labName = laboratory.labName.labNameInfo,
                labDescription = laboratory.labDescription.labDescriptionInfo,
                labDuration = laboratory.labDuration.labDurationInfo?.toInt(DurationUnit.MINUTES),
                labQueueLimit = laboratory.labQueueLimit.labQueueLimitInfo,
                createdAt = laboratory.createdAt.toString(),
                ownerId = laboratory.ownerId,
                groups =
                    laboratory.groups.map { group ->
                        GroupOutputModel(
                            id = group.id,
                            groupName = group.groupName.groupNameInfo,
                            groupDescription = group.groupDescription.groupDescriptionInfo,
                            ownerId = group.ownerId,
                            createdAt = group.createdAt.toString(),
                        )
                    },
            )
    }
}
