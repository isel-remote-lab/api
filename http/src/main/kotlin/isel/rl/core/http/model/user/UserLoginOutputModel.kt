package isel.rl.core.http.model.user

import isel.rl.core.domain.user.User

data class UserLoginOutputModel(
    val token: String,
    val user: UserOutputModel,
) {
    companion object {
        fun mapOf(
            token: String,
            user: User,
        ) = mapOf(
            "token" to token,
            "user" to UserOutputModel.toOutputModel(user),
        )
    }
}
