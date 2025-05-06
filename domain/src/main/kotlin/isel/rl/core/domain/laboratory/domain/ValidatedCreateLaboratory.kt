package isel.rl.core.domain.laboratory.domain

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import kotlinx.datetime.Instant

data class ValidatedCreateLaboratory internal constructor(
    val labName: LabName,
    val labDescription: LabDescription,
    val labDuration: LabDuration,
    val labQueueLimit: LabQueueLimit,
    val createdAt: Instant,
    val ownerId: Int,
)
