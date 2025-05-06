package isel.rl.core.http.model.laboratory

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class LaboratoryUpdateInputModel(
    val labName: String?,
    val labDescription: String?,
    val labDuration: Int?,
    val labQueueLimit: Int?,
)
