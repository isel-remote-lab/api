package repository

import domain.user.Email
import domain.user.PasswordValidationInfo
import domain.user.User
import kotlinx.datetime.Instant

/**
 * Repository for users.
 */
interface UserRepository {
    fun createUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
        email: Email,
        studentNr: Int,
        createdAt: Instant
    ): Int

    fun getUserById(userId: Int): User?

    fun getUserByEmail(email: Email): User?

    fun getUserByStudentNr(studentNr: Int): User?

    // TODO: Check if the returned type is fine!
    fun updateUserUsername(
        userId: Int,
        username: String
    ): Boolean

    // TODO: Check if the returned type is fine!
    fun deleteUser(userId: Int): Boolean
}