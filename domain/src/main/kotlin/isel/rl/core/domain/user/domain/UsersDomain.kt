package isel.rl.core.domain.user.domain

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.ValidatedUser
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class UsersDomain {
    fun validateCreateUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
        createdAt: Instant,
    ): ValidatedUser = ValidatedUser(
        checkOAuthId(oauthId),
        checkRole(role),
        checkUsername(username),
        checkEmail(email),
        createdAt,
    )

    fun validateUserId(userId: String): Int =
        try {
            userId.toInt()
        } catch (e: Exception) {
            throw ServicesExceptions.Users.InvalidUserId
        }

    fun checkUsername(username: String): Username =
        if (username.isBlank()) {
            throw ServicesExceptions.Users.InvalidUsername
        } else {
            Username(username)
        }

    fun checkEmail(email: String): Email =
        if (email.isBlank()) {
            throw ServicesExceptions.Users.InvalidEmail
        } else {
            Email(email)
        }


    fun checkOAuthId(oauthId: String): OAuthId =
        if (oauthId.isBlank()) {
            throw ServicesExceptions.Users.InvalidOauthId
        } else {
            OAuthId(oauthId)
        }


    fun checkRole(role: String): Role =
        when (role.uppercase(Locale.getDefault())) {
            "S" -> Role.STUDENT
            "T" -> Role.TEACHER
            "A" -> Role.ADMIN
            else -> throw ServicesExceptions.Users.InvalidRole
        }

}
