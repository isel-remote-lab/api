package isel.rl.core.repository

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.ValidatedUser
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.token.Token
import isel.rl.core.domain.user.token.TokenValidationInfo
import kotlinx.datetime.Instant

/**
 * Repository for users.
 */
interface UsersRepository {
    fun createUser(user: ValidatedUser): Int

    fun getUserById(userId: Int): User?

    fun getUserByEmail(email: Email): User?

    fun updateUserRole(
        userId: Int,
        role: Role,
    ): Boolean

    /**
     * Creates a new token for a user.
     * @param token the token to be created
     * @param maxTokens the maximum number of tokens allowed
     */
    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun getUserByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun deleteUser(userId: Int): Boolean
}
