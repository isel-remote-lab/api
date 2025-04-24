package isel.rl.core.domain.laboratory

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit

data class ValidatedUpdateLaboratory internal constructor(
    val labId: Int,
    val labName: LabName?,
    val labDescription: LabDescription?,
    val labDuration: LabDuration?,
    val labQueueLimit: LabQueueLimit?,
)
