package rl.domain.user.domain

import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import rl.domain.config.UsersDomainConfig
import rl.domain.user.*
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

    fun checkUsername(username: String): Username {
        return if (username.isBlank()) {
            throw UsersDomainException.InvalidUsername
        } else {
            Username(username)
        }
    }

    fun checkEmail(email: String): Email {
        return if (email.isBlank()) {
            throw UsersDomainException.InvalidEmail
        } else {
            Email(email)
        }
    }

    fun checkOAuthId(oauthId: String): OAuthId {
        return if (oauthId.isBlank()) {
            throw UsersDomainException.InvalidOauthId
        } else {
            OAuthId(oauthId)
        }
    }

    fun checkRole(role: String): Role {
        return when (role.uppercase(Locale.getDefault())) {
            "S" -> Role.STUDENT
            "T" -> Role.TEACHER
            "A" -> Role.ADMIN
            else -> throw UsersDomainException.InvalidRole
        }
    }
}