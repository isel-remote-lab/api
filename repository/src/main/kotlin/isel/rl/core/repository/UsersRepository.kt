package isel.rl.core.repository

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.ValidatedUser
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Username
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

    fun getUserByOAuthId(oauthId: OAuthId): User?

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

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun updateUserUsername(
        userId: Int,
        username: Username,
    ): User

    fun deleteUser(userId: Int): Boolean
}
