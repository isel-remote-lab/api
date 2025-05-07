package isel.rl.core.domain.laboratory

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import kotlinx.datetime.Instant

data class Laboratory(
    val id: Int,
    val labName: LabName,
    val labDescription: LabDescription,
    val labDuration: LabDuration,
    val labQueueLimit: LabQueueLimit,
    val createdAt: Instant,
    val ownerId: Int,
) {
    companion object {
        const val ID_PROP = "id"
        const val LAB_NAME_PROP = "lab_name"
        const val LAB_DESCRIPTION_PROP = "lab_description"
        const val LAB_DURATION_PROP = "lab_duration"
        const val LAB_QUEUE_LIMIT_PROP = "lab_queue_limit"
        const val CREATED_AT_PROP = "created_at"
        const val OWNER_ID_PROP = "owner_id"
    }
}
