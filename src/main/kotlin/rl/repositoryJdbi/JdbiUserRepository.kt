package rl.repositoryJdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import rl.domain.user.Email
import rl.domain.user.PasswordValidationInfo
import rl.domain.user.User
import rl.repository.UserRepository

data class JdbiUserRepository(
    val handle: Handle
): UserRepository {
    override fun createUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
        email: Email,
        studentNr: Int,
        createdAt: Instant
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getUserById(userId: Int): User? {
        TODO("Not yet implemented")
    }

    override fun getUserByEmail(email: Email): User? {
        TODO("Not yet implemented")
    }

    override fun getUserByStudentNr(studentNr: Int): User? {
        TODO("Not yet implemented")
    }

    override fun updateUserUsername(userId: Int, username: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteUser(userId: Int): Boolean {
        TODO("Not yet implemented")
    }

}