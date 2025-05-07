package isel.rl.core.http.model.laboratory

import isel.rl.core.domain.laboratory.Laboratory
import kotlin.time.DurationUnit

data class LaboratoryOutputModel(
    val id: Int,
    val labName: String,
    val labDescription: String,
    val labDuration: Int,
    val labQueueLimit: Int,
    val ownerId: Int,
    val createdAt: String,
) {
    companion object {
        fun mapOf(lab: Laboratory) =
            mapOf(
                "laboratory" to
                    LaboratoryOutputModel(
                        id = lab.id,
                        labName = lab.labName.labNameInfo,
                        labDescription = lab.labDescription.labDescriptionInfo,
                        labDuration = lab.labDuration.labDurationInfo.toInt(DurationUnit.MINUTES),
                        labQueueLimit = lab.labQueueLimit.labQueueLimitInfo,
                        ownerId = lab.ownerId,
                        createdAt = lab.createdAt.toString(),
                    ),
            )
    }
}
