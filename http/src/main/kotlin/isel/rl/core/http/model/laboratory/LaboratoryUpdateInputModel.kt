package isel.rl.core.http.model.laboratory

data class LaboratoryUpdateInputModel(
    val name: String?,
    val description: String?,
    val duration: Int?,
    val queueLimit: Int?,
)
