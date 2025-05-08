package isel.rl.core.http.model.user

import isel.rl.core.domain.user.User

data class UserOutputModel(
    val id: Int,
    val role: String,
    val name: String,
    val email: String,
    val createdAt: String,
) {
    companion object {
        fun toOutputModel(user: User) =
            UserOutputModel(
                id = user.id,
                role = user.role.char,
                name = user.name.nameInfo,
                email = user.email.emailInfo,
                createdAt = user.createdAt.toString(),
            )

        fun mapOf(user: User) =
            mapOf(
                "user" to
                    UserOutputModel(
                        id = user.id,
                        role = user.role.char,
                        name = user.name.nameInfo,
                        email = user.email.emailInfo,
                        createdAt = user.createdAt.toString(),
                    ),
            )
    }
}
