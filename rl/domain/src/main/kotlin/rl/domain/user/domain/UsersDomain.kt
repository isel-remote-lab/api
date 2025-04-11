package rl.domain.user.domain

import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import rl.domain.exceptions.ServicesExceptions
import rl.domain.user.*
import rl.domain.user.props.*
import java.util.*

@Component
class UsersDomain(
    //private val usersDomainConfig: UsersDomainConfig
) {
    fun validateCreateUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
        createdAt: Instant
    ): ValidatedUser {
        val checkedRole = checkRole(role)
        val checkedUsername = checkUsername(username)
        val checkedEmail = checkEmail(email)
        val checkedOauthId = checkOAuthId(oauthId)

        return ValidatedUser(
            checkedOauthId,
            checkedRole,
            checkedUsername,
            checkedEmail,
            createdAt
        )
    }

    fun validateUserId(userId: String): Int {
        try {
            return userId.toInt()
        } catch (e: Exception) {
            throw ServicesExceptions.Users.InvalidUserId
        }
    }

    fun checkUsername(username: String): Username {
        return if (username.isBlank()) {
            throw ServicesExceptions.Users.InvalidUsername
        } else {
            Username(username)
        }
    }

    fun checkEmail(email: String): Email {
        return if (email.isBlank()) {
            throw ServicesExceptions.Users.InvalidEmail
        } else {
            Email(email)
        }
    }

    fun checkOAuthId(oauthId: String): OAuthId {
        return if (oauthId.isBlank()) {
            throw ServicesExceptions.Users.InvalidOauthId
        } else {
            OAuthId(oauthId)
        }
    }

    fun checkRole(role: String): Role {
        return when (role.uppercase(Locale.getDefault())) {
            "S" -> Role.STUDENT
            "T" -> Role.TEACHER
            "A" -> Role.ADMIN
            else -> throw ServicesExceptions.Users.InvalidRole
        }
    }
}