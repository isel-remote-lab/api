package isel.rl.core.domain.user.domain

import isel.rl.core.domain.config.UsersDomainConfig
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.domain.user.token.Token
import isel.rl.core.domain.user.token.TokenEncoder
import isel.rl.core.domain.user.token.TokenValidationInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale

@Component
class UsersDomain(
    private val domainConfig: UsersDomainConfig,
    private val tokenEncoder: TokenEncoder,
) {
    /**
     * Generates a token value.
     *
     * @return the generated token value
     */
    fun generateTokenValue(): String =
        ByteArray(domainConfig.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    /**
     * Checks if the token can be a valid token.
     *
     * @param token the token to check
     * @return `true` if the token can be a valid token, `false` otherwise
     */
    fun canBeToken(token: String): Boolean =
        try {
            Base64.getUrlDecoder()
                .decode(token).size == domainConfig.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    /**
     * Checks if the token time is valid.
     *
     * @param clock the clock
     * @param token the token to check
     * @return `true` if the token time is valid, `false` otherwise
     */
    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
            (now - token.createdAt) <= domainConfig.tokenTtl &&
            (now - token.lastUsedAt) <= domainConfig.tokenRollingTtl
    }

    /**
     * Gets the token expiration.
     *
     * @param token the token to get the expiration for
     * @return the token expiration
     */
    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + domainConfig.tokenTtl
        val rollingExpiration = token.lastUsedAt + domainConfig.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    /**
     * Creates a token validation information.
     *
     * @param token the token to create the validation information for
     * @return the created token validation information
     */
    fun createTokenValidationInformation(token: String): TokenValidationInfo = tokenEncoder.createValidationInformation(token)

    /**
     * The maximum number of tokens per user.
     */
    val maxNumberOfTokensPerUser = domainConfig.maxTokensPerUser

    fun validateCreateUser(
        role: String,
        username: String,
        email: String,
        createdAt: Instant,
    ): ValidatedUser =
        ValidatedUser(
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

    fun checkRole(role: String): Role =
        when (role.uppercase(Locale.getDefault())) {
            "S" -> Role.STUDENT
            "T" -> Role.TEACHER
            "A" -> Role.ADMIN
            else -> throw ServicesExceptions.Users.InvalidRole
        }
}
