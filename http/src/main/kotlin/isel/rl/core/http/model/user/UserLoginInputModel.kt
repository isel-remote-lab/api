package isel.rl.core.http.model.user

data class UserLoginInputModel(
    val oauthId: String,
    val username: String,
    val email: String,
    val accessToken: String,
)
