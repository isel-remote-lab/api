package rl.repository

import rl.RepoUtils
import rl.TestClock
import rl.domain.user.Role
import rl.domain.user.token.Token
import rl.domain.user.token.TokenValidationInfo
import rl.repositoryJdbi.JdbiUserRepository
import kotlin.test.*

class JdbiUserRepositoryTests {
    @Test
    fun `store student and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val userId = userRepo.createUser(userRole, username, email, createdAt)

            // when: retrieving a user by Id
            val userById = userRepo.getUserById(userId)

            // then:
            assertNotNull(userById) { "No user retrieved from database" }
            assertEquals(username, userById.username)
            assertEquals(email, userById.email)
            assertTrue(userById.id >= 0)

            // when: retrieving a user by email
            val userByEmail = userRepo.getUserByEmail(email)

            // then:
            assertNotNull(userByEmail) { "No user retrieved from database" }
            assertEquals(username, userByEmail.username)
            assertEquals(email, userByEmail.email)
            assertTrue(userByEmail.id >= 0)
        }
    }

    @Test
    fun `update username`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val userId = userRepo.createUser(userRole, username, email, createdAt)

            // when: updating username
            val newUsername = repoUtils.newTestUsername()
            val userWithNewUsername = userRepo.updateUserUsername(userId, newUsername)

            // then:
            assertEquals(newUsername, userWithNewUsername.username)
            assertNotEquals(username, userWithNewUsername.username)
        }
    }

    @Test
    fun `delete user`() {
        repoUtils.runWithHandle { handle ->
            // given: a user repo
            val userRepo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // when: storing a user
            val username = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val userId = userRepo.createUser(userRole, username, email, createdAt)

            // when: deleting a user
            userRepo.deleteUser(userId)

            // then: try to get the user
            val deletedUser = userRepo.getUserById(userId)
            assertNull(deletedUser)
        }
    }

    @Test
    fun `can create and validate tokens`() {
        repoUtils.runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUserRepository(handle)
            // and: a test clock
            val clock = TestClock()

            // and: a createdUser
            val userName = repoUtils.newTestUsername()
            val email = repoUtils.newTestEmail()
            val createdAt = clock.now()
            val userRole = repoUtils.randomUserRole()
            val userId = repo.createUser(userRole, userName, email, createdAt)

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
            assertEquals(userName, user.username)
            assertEquals(testTokenValidationInfo.validationInfo, retrievedToken.tokenValidationInfo.validationInfo)
            assertEquals(tokenCreationInstant, retrievedToken.createdAt)
        }
    }


    companion object {
        private val repoUtils = RepoUtils()
    }
}