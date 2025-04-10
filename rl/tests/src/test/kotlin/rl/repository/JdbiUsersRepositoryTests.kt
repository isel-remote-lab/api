package rl.repository

import rl.TestClock
import rl.domain.user.UserFactory
import rl.jdbi.JdbiUsersRepository
import kotlin.test.*

class JdbiUsersRepositoryTests {
    @Test
    fun `store student and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val oAuthId = repoUtils.newTestOauthId()
            val userId = userRepo.createUser(
                UserFactory.createValidatedUser(
                    oAuthId, userRole, username, email, createdAt
                )
            )

            // when: retrieving a user by Id
            val userById = userRepo.getUserById(userId)

            // then: verify the retrieved user details
            assertNotNull(userById) { "No user retrieved from database" }
            assertEquals(username, userById.username)
            assertEquals(email, userById.email)
            assertEquals(createdAt, userById.createdAt)
            assertEquals(userRole, userById.role)
            assertEquals(oAuthId, userById.oauthId)
            assertTrue(userById.id >= 0)

            // when: retrieving a user by email
            val userByEmail = userRepo.getUserByEmail(email)

            // then: verify the retrieved user details
            assertNotNull(userByEmail) { "No user retrieved from database" }
            assertEquals(username, userByEmail.username)
            assertEquals(email, userByEmail.email)
            assertEquals(createdAt, userByEmail.createdAt)
            assertEquals(userRole, userByEmail.role)
            assertEquals(oAuthId, userByEmail.oauthId)
            assertTrue(userByEmail.id >= 0)
        }
    }

    @Test
    fun `update username`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val oAuthId = repoUtils.newTestOauthId()
            val userId = userRepo.createUser(
                UserFactory.createValidatedUser(
                    oAuthId, userRole, username, email, createdAt
                )
            )

            // when: updating username
            val newUsername = repoUtils.newTestUsername()
            val userWithNewUsername = userRepo.updateUserUsername(userId, newUsername)

            // then: verify the updated username
            assertEquals(newUsername, userWithNewUsername.username)
            assertNotEquals(username, userWithNewUsername.username)
        }
    }

    @Test
    fun `delete user`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUsersRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val oAuthId = repoUtils.newTestOauthId()
            val userId = userRepo.createUser(
                UserFactory.createValidatedUser(
                    oAuthId, userRole, username, email, createdAt
                )
            )

            // when: deleting a user
            userRepo.deleteUser(userId)

            // then: try to get the user
            val deletedUser = userRepo.getUserById(userId)
            assertNull(deletedUser)
        }
    }

    /*
    @Test
    fun `can create and validate tokens`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val repo = JdbiUsersRepository(handle)
            // and: a test clock
            val clock = TestClock()

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
    }
}