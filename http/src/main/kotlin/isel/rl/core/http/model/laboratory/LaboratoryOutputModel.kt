package isel.rl.core.http.model.laboratory

data class LaboratoryOutputModel(
    val id: Int,
    val labName: String,
    val labDescription: String,
    val labDuration: Int,
    val labQueueLimit: Int,
    val ownerId: Int,
    val createdAt: String,
)
