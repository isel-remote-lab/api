package isel.rl.core.http.model.laboratory

import isel.rl.core.domain.laboratory.Laboratory
import kotlin.time.DurationUnit

data class LaboratoryOutputModel(
    val id: Int,
    val name: String?,
    val description: String?,
    val duration: Int?,
    val queueLimit: Int?,
    val ownerId: Int,
    val createdAt: String,
) {
    companion object {
        fun mapOf(lab: Laboratory) =
            LaboratoryOutputModel(
                id = lab.id,
                name = lab.name.labNameInfo,
                description = lab.description.labDescriptionInfo,
                duration = lab.duration.labDurationInfo?.toInt(DurationUnit.MINUTES),
                queueLimit = lab.queueLimit.labQueueLimitInfo,
                ownerId = lab.ownerId,
                createdAt = lab.createdAt.toString(),
            )
    }
}
