package isel.rl.core.http.model.user

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import isel.rl.core.domain.user.User

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserOutputModel(
    val id: Int,
    val oAuthId: String,
    val role: String,
    val username: String,
    val email: String,
    val createdAt: String,
) {
    companion object {
        fun mapOf(user: User) =
            mapOf(
                "user" to
                    UserOutputModel(
                        id = user.id,
                        oAuthId = user.oAuthId.oAuthIdInfo,
                        role = user.role.char,
                        username = user.username.usernameInfo,
                        email = user.email.emailInfo,
                        createdAt = user.createdAt.toString(),
                    ),
            )
    }
}
