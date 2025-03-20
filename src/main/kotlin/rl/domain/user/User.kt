package domain.user

import kotlinx.datetime.Instant

data class User(
    val userId: Int,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
    val email: Email,
    val studentNr: Int,
    val createdAt: Instant
)