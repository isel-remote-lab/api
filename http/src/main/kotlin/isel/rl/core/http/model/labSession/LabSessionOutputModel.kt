package isel.rl.core.http.model.labSession

import isel.rl.core.domain.laboratory.session.LabSession

data class LabSessionOutputModel(
    val id: String,
    val labId: String,
    val ownerId: String,
    val startTime: String,
    val endTime: String,
    val state: String,
) {
    companion object {
        fun mapOf(labSession: LabSession): LabSessionOutputModel =
            LabSessionOutputModel(
                id = labSession.id.toString(),
                labId = labSession.labId.toString(),
                ownerId = labSession.ownerId.toString(),
                startTime = labSession.startTime.toString(),
                endTime = labSession.endTime.toString(),
                state = labSession.state.char,
            )
    }
}
