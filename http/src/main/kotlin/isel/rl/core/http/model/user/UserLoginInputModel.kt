package isel.rl.core.http.model.user

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class UserLoginInputModel(
    val oauthId: String,
    val username: String,
    val email: String,
)
