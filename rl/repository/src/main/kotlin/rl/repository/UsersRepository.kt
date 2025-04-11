package rl.repository

import kotlinx.datetime.Instant
import rl.domain.user.User
import rl.domain.user.ValidatedUser
import rl.domain.user.props.Email
import rl.domain.user.props.Username
import rl.domain.user.token.Token
import rl.domain.user.token.TokenValidationInfo

/**
 * Repository for users.
 */
interface UsersRepository {
    fun createUser(
        user: ValidatedUser
    ): Int

    fun getUserById(userId: Int): User?

    fun getUserByEmail(email: Email): User?

    /**
     * Creates a new token for a user.
     * @param token the token to be created
     * @param maxTokens the maximum number of tokens allowed
     */
    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(token: Token, now: Instant)

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun updateUserUsername(
        userId: Int,
        username: Username
    ): User

    fun deleteUser(userId: Int): Boolean
}