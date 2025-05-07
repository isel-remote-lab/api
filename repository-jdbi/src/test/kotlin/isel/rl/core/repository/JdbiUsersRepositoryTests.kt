package isel.rl.core.repository

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.domain.user.token.Token
import isel.rl.core.domain.user.token.TokenValidationInfo
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JdbiUsersRepositoryTests {
    @Test
    fun `store student and retrieve it by id, email and oauthId`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: retrieving a user by Id
            val userById = userRepo.getUserById(userId)

            // then: verify the retrieved user details
            initialUser.assertUserWith(userById)

            // when: retrieving a user by email
            val userByEmail = userRepo.getUserByEmail(initialUser.email)

            // then: verify the retrieved user details
            initialUser.assertUserWith(userByEmail)
        }
    }

    @Test
    fun `delete user`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo and user domain
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: deleting a user
            userRepo.deleteUser(userId)

            // then: try to get the user
            val deletedUser = userRepo.getUserById(userId)
            assertNull(deletedUser, "User should be deleted")
        }
    }

    @Test
    fun `create token and get user by token validation info`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: creating a token
            val tokenValidationInfo = TokenValidationInfo(repoUtils.newTokenValidationData())
            val createdAt = clock.now()
            val lastUsedAt = clock.now()
            val token = Token(tokenValidationInfo, userId, createdAt, lastUsedAt)
            userRepo.createToken(token, 1)

            // then: get user by token validation info
            val userAndToken = userRepo.getUserByTokenValidationInfo(tokenValidationInfo)

            // then:
            // Assert User
            assertNotNull(userAndToken)
            assertEquals(userId, userAndToken.first.id)
            assertEquals(initialUser.username, userAndToken.first.username)
            assertEquals(initialUser.email, userAndToken.first.email)
            assertEquals(initialUser.createdAt, userAndToken.first.createdAt)
            assertEquals(initialUser.userRole, userAndToken.first.role)

            // Assert Token
            assertEquals(token, userAndToken.second)
        }
    }

    @Test
    fun `update token last_used_at and get token by validation info`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: creating a token
            val tokenValidationInfo = TokenValidationInfo(repoUtils.newTokenValidationData())
            val createdAt = clock.now()
            val lastUsedAt = clock.now()
            val token = Token(tokenValidationInfo, userId, createdAt, lastUsedAt)
            userRepo.createToken(token, 1)

            Thread.sleep(2000)

            val updatedAt = clock.now()
            userRepo.updateTokenLastUsed(token, updatedAt)
            val userAndToken = userRepo.getUserByTokenValidationInfo(tokenValidationInfo)

            assertNotNull(userAndToken)
            assertEquals(token, userAndToken.second)
        }
    }

    @Test
    fun `remove token by validation info`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: creating a token
            val tokenValidationInfo = TokenValidationInfo(repoUtils.newTokenValidationData())
            val createdAt = clock.now()
            val lastUsedAt = clock.now()
            val token = Token(tokenValidationInfo, userId, createdAt, lastUsedAt)
            userRepo.createToken(token, 1)

            val tokenRetrieved = userRepo.getUserByTokenValidationInfo(tokenValidationInfo)

            assertNotNull(tokenRetrieved)
            assertEquals(token, tokenRetrieved.second)

            val nrOfTokensDeleted = userRepo.removeTokenByValidationInfo(tokenValidationInfo)
            assertEquals(1, nrOfTokensDeleted)

            val tryRetrieveToken = userRepo.getUserByTokenValidationInfo(tokenValidationInfo)
            assertNull(tryRetrieveToken)
        }
    }

    companion object {
        private val repoUtils = RepoUtils()

        /**
         * This class represents the initial user information used for testing.
         */
        private data class InitialUserInfo(
            val clock: TestClock,
            val username: Username = repoUtils.newTestUsername(),
            val email: Email = repoUtils.newTestEmail(),
            val createdAt: Instant = clock.now(),
            val userRole: Role = repoUtils.randomUserRole(),
        )

        /**
         * Asserts that the given user matches the initial user information.
         *
         * @param user The user to assert against.
         */
        private fun InitialUserInfo.assertUserWith(user: User?) {
            assertNotNull(user) { "No user retrieved" }
            assertEquals(username, user.username, "Usernames do not match")
            assertEquals(email, user.email, "Emails do not match")
            assertEquals(createdAt, user.createdAt, "CreatedAt do not match")
            assertEquals(userRole, user.role, "Roles do not match")
            assertTrue(user.id >= 0, "UserId must be >= 0")
        }

        /**
         * Creates a [user] in the [JdbiUsersRepository].
         *
         * @param user The initial user information.
         * @return The ID of the created user.
         */
        private fun JdbiUsersRepository.createUser(user: InitialUserInfo): Int =
            createUser(
                repoUtils.usersDomain.validateCreateUser(
                    user.userRole.char,
                    user.username.usernameInfo,
                    user.email.emailInfo,
                    user.createdAt,
                ),
            )
    }
}
