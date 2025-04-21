package isel.rl.core.http.model.user

data class UserOutputModel(
    val id: Int,
    val oauthId: String,
    val role: String,
    val username: String,
    val email: String,
    val createdAt: String,
)
