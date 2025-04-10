package rl.domain.user

import kotlinx.datetime.Instant

sealed class UserType(
    val oauthId: OAuthId,
    val role: Role,
    val username: Username,
    val email: Email,
    val createdAt: Instant
)

data class Email(val emailInfo: String)
data class OAuthId(val oAuthIdInfo: String)
data class Username(val usernameInfo: String)

enum class Role(val char: String) {
    STUDENT("S"),
    TEACHER("T"),
    ADMIN("A");
}