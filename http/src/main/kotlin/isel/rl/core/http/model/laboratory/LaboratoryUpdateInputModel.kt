package isel.rl.core.http.model.laboratory

data class LaboratoryUpdateInputModel(
    val labName: String?,
    val labDescription: String?,
    val labDuration: Int?,
    val labQueueLimit: Int?,
)
