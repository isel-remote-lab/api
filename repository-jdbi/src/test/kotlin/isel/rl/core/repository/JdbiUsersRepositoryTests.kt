package isel.rl.core.repository

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

            // when: retrieving a user by oauthId
            val userByOauthId = userRepo.getUserByOAuthId(initialUser.oAuthId)

            // then: verify the retrieved user details
            initialUser.assertUserWith(userByOauthId)
        }
    }

    @Test
    fun `update username`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo and user domain
            val userRepo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val initialUser = InitialUserInfo(clock)
            val userId = userRepo.createUser(initialUser)

            // when: updating username
            val newUsername = repoUtils.newTestUsername()
            val userWithNewUsername = userRepo.updateUserUsername(userId, newUsername)

            // then: verify the updated username
            assertEquals(newUsername, userWithNewUsername.username, "Usernames do not match")
            assertNotEquals(initialUser.username, userWithNewUsername.username, "Usernames should be different")
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

    /*
    @Test
    fun `can create and validate tokens`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val repo = JdbiUsersRepository(handle)
            // and: a test clock
            val clock = isel.rl.core.repository.utils.TestClock()

            // and: a createdUser
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val oAuthId = repoUtils.newTestOauthId()
            val userId = repo.createUser(
                UserFactory.createValidatedUser(
                    oAuthId, userRole, username, email, createdAt
                )
            )

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(repoUtils.newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                userId,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: retrieving the token and associated user
            val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

            // then:
            val (user, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

            // and: ...
            assertEquals(username, user.username)
            assertEquals(testTokenValidationInfo.validationInfo, retrievedToken.tokenValidationInfo.validationInfo)
            assertEquals(tokenCreationInstant, retrievedToken.createdAt)
        }
    }
     */

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
            val oAuthId: OAuthId = repoUtils.newTestOauthId(),
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
            assertEquals(oAuthId, user.oauthId, "OAuthIds do not match")
            assertTrue(user.id >= 0, "UserId must be >= 0")
        }

        /**
         * Creates a [user] in the [JdbiUsersRepository].
         *
         * @param user The initial user information.
         * @return The ID of the created user.
         */
        private fun JdbiUsersRepository.createUser(user: InitialUserInfo): Int {
            val usersDomain = UsersDomain()

            return createUser(
                usersDomain.validateCreateUser(
                    user.oAuthId.oAuthIdInfo,
                    user.userRole.char,
                    user.username.usernameInfo,
                    user.email.emailInfo,
                    user.createdAt,
                ),
            )
        }
    }
}
