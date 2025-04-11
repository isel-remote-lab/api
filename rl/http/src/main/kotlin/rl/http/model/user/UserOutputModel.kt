package rl.http.model.user

data class UserOutputModel(
    val id: String,
    val oauthId: String,
    val role: String,
    val username: String,
    val email: String,
    val createdAt: String,
)
