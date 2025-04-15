package isel.rl.core.http.model.user

data class UserCreateInputModel(
    val oauthId: String,
    val role: String,
    val username: String,
    val email: String,
)
