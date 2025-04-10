package rl.http.model

data class UserCreateInputModel(
    val oauthId: String,
    val role: String,
    val username: String,
    val email: String
)
