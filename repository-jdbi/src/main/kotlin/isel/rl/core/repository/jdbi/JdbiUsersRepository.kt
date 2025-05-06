package isel.rl.core.repository.jdbi

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.ValidatedUser
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.domain.user.token.Token
import isel.rl.core.domain.user.token.TokenValidationInfo
import isel.rl.core.repository.UsersRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory

data class JdbiUsersRepository(
    val handle: Handle,
) : UsersRepository {
    override fun createUser(user: ValidatedUser): Int =
        handle.createUpdate(
            """
           INSERT INTO rl.user (o_auth_id, role, username, email, created_at)
           VALUES (:o_auth_id, :role, :username, :email, :created_at)
           """,
        )
            .bind("o_auth_id", user.oauthId.oAuthIdInfo)
            .bind("role", user.role.char)
            .bind("username", user.username.usernameInfo)
            .bind("email", user.email.emailInfo)
            .bind("created_at", user.createdAt.toJavaInstant())
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getUserById(userId: Int): User? =
        handle.createQuery("SELECT * FROM rl.user WHERE id = :id")
            .bind("id", userId)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserByEmail(email: Email): User? =
        handle.createQuery("SELECT * FROM rl.user WHERE email = :email")
            .bind("email", email.emailInfo)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserByOAuthId(oauthId: OAuthId): User? =
        handle.createQuery("SELECT * FROM rl.user WHERE o_auth_id = :o_auth_id")
            .bind("o_auth_id", oauthId.oAuthIdInfo)
            .mapTo<User>()
            .singleOrNull()

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val deletions =
            handle.createUpdate(
                """
                DELETE FROM rl.token 
                WHERE user_id = :user_id 
                    AND token_validation IN (
                        SELECT token_validation FROM rl.token WHERE user_id = :user_id 
                            ORDER BY last_used_at DESC OFFSET :offset
                    )
                """.trimIndent(),
            )
                .bind("user_id", token.userId)
                .bind("offset", maxTokens - 1)
                .execute()

        logger.info("{} tokens deleted when creating new token", deletions)

        handle.createUpdate(
            """
            INSERT INTO rl.token(user_id, token_validation, created_at, last_used_at) 
            VALUES (:user_id, :token_validation, :created_at, :last_used_at)
            """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.toJavaInstant())
            .bind("last_used_at", token.lastUsedAt.toJavaInstant())
            .execute()
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle.createUpdate(
            """
            UPDATE rl.token
            SET last_used_at = :last_used_at
            WHERE token_validation = :validation_information
            """.trimIndent(),
        )
            .bind("last_used_at", now.toJavaInstant())
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun getUserByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle.createQuery(
            """
                SELECT users.id, 
                users.o_auth_id,
                users.role,
                users.username, 
                users.email, 
                users.created_at AS user_created_at, 
                tokens.token_validation, 
                tokens.created_at AS token_created_at, 
                tokens.last_used_at
                FROM rl.user AS users 
                INNER JOIN rl.token AS tokens 
                ON users.id = tokens.user_id
                WHERE tokens.token_validation = :validation_information;
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()
            ?.userAndToken

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle.createUpdate(
            """
                DELETE FROM rl.token
                WHERE token_validation = :validation_information
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()

    override fun updateUserUsername(
        userId: Int,
        username: Username,
    ): User {
        handle.createUpdate(
            """
           UPDATE rl.user
           SET username = :username
           WHERE id = :id
        """,
        )
            .bind("username", username.usernameInfo)
            .bind("id", userId)
            .execute()

        return getUserById(userId)!!
    }

    override fun deleteUser(userId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.user
            WHERE id = :id
        """,
        )
            .bind("id", userId)
            .execute() == 1 // Check if 1 deletion was perform

    private data class UserAndTokenModel(
        val id: Int,
        val oAuthId: String,
        val role: String,
        val username: String,
        val email: String,
        val userCreatedAt: Instant,
        val tokenValidation: String,
        val tokenCreatedAt: Instant,
        val lastUsedAt: Instant,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(
                        id,
                        OAuthId(oAuthId),
                        Role.entries.firstOrNull { it.char == role }!!,
                        Username(username),
                        Email(email),
                        userCreatedAt,
                    ),
                    Token(
                        TokenValidationInfo(tokenValidation),
                        id,
                        tokenCreatedAt,
                        lastUsedAt,
                    ),
                )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
