package isel.rl.core.http.model.laboratory

data class LaboratoryCreateInputModel(
    val labName: String,
    val labDescription: String,
    val labDuration: Int,
    val labQueue: Int,
    val ownerId: Int,
)