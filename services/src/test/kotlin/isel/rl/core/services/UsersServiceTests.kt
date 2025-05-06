package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.Either
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class UsersServiceTests {
    @Test
    fun `create user and get by id, email and oauthId`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: creating a user
        val username = servicesUtils.newTestUsername()
        val email = servicesUtils.newTestEmail()
        val userRole = servicesUtils.randomUserRole()
        val oAuthId = servicesUtils.newTestOauthId()
        val createdUserResult =
            service.createUser(
                oAuthId,
                userRole,
                username,
                email,
            )

        // then: Verify if the user was created
        when (createdUserResult) {
            is Either.Left -> fail("User creation failed: ${createdUserResult.value}")
            is Either.Right ->
                assertTrue(
                    createdUserResult.value.id >= 0,
                    CREATE_ID_ERROR,
                )
        }

        val userId = createdUserResult.value.id

        // when: getting the user by id
        // then: Verify if the user was retrieved by id
        when (val userByIdResult = service.getUserById(userId.toString())) {
            is Either.Left -> fail("User retrieval by id failed: ${userByIdResult.value}")
            is Either.Right -> {
                val user = userByIdResult.value
                assertEquals(user.id, userId, GET_USER_ID_ERROR)
                assertEquals(user.username.usernameInfo, username, GET_USERNAME_ERROR)
                assertEquals(user.email.emailInfo, email, GET_USER_EMAIL_ERROR)
                assertEquals(user.role.char, userRole, GET_USER_ROLE_ERROR)
                assertEquals(user.oAuthId.oAuthIdInfo, oAuthId, GET_USER_OAUTHID_ERROR)
                assertEquals(user.createdAt, clock.now(), GET_USER_CREATEDAT_ERROR)
            }
        }

        // when: getting the user by email
        // then: Verify if the user was retrieved by email
        when (val userByEmailResult = service.getUserByEmailOrAuthId(email = email)) {
            is Either.Left -> fail("User retrieval by email failed: ${userByEmailResult.value}")
            is Either.Right -> {
                val user = userByEmailResult.value
                assertEquals(user.id, userId, GET_USER_ID_ERROR)
                assertEquals(user.username.usernameInfo, username, GET_USERNAME_ERROR)
                assertEquals(user.email.emailInfo, email, GET_USER_EMAIL_ERROR)
                assertEquals(user.role.char, userRole, GET_USER_ROLE_ERROR)
                assertEquals(user.oAuthId.oAuthIdInfo, oAuthId, GET_USER_OAUTHID_ERROR)
                assertEquals(user.createdAt, clock.now(), GET_USER_CREATEDAT_ERROR)
            }
        }

        // when: getting the user by oauthId
        // then: Verify if the user was retrieved by oauthId
        when (val userByOauthIdResult = service.getUserByEmailOrAuthId(oAuthId = oAuthId)) {
            is Either.Left -> fail("User retrieval by oauthId failed: ${userByOauthIdResult.value}")
            is Either.Right -> {
                val user = userByOauthIdResult.value
                assertEquals(user.id, userId, GET_USER_ID_ERROR)
                assertEquals(user.username.usernameInfo, username, GET_USERNAME_ERROR)
                assertEquals(user.email.emailInfo, email, GET_USER_EMAIL_ERROR)
                assertEquals(user.role.char, userRole, GET_USER_ROLE_ERROR)
                assertEquals(user.oAuthId.oAuthIdInfo, oAuthId, GET_USER_OAUTHID_ERROR)
                assertEquals(user.createdAt, clock.now(), GET_USER_CREATEDAT_ERROR)
            }
        }
    }

    @Test
    fun `get user by invalid id return the right exceptions`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: getting a user by a non number id
        val invalidId = "invalidId"
        when (val result = service.getUserById(invalidId)) {
            is Either.Left -> assertTrue(result.value is ServicesExceptions.Users.InvalidUserId)
            is Either.Right -> fail("Expected an error, but got a user: ${result.value}")
        }

        // when: getting a user by a negative id
        val negativeId = -1
        when (val result = service.getUserById(negativeId.toString())) {
            is Either.Left -> assertTrue(result.value is ServicesExceptions.Users.UserNotFound)
            is Either.Right -> fail("Expected an error, but got a user: ${result.value}")
        }

        // when: getting a user by a non existent id
        val nonExistentId = 999999
        when (val result = service.getUserById(nonExistentId.toString())) {
            is Either.Left -> assertTrue(result.value is ServicesExceptions.Users.UserNotFound)
            is Either.Right -> fail("Expected an error, but got a user: ${result.value}")
        }
    }

    @Test
    fun `get user by invalid email and oauth id return the right exceptions`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        val invalidEmail = " "
        val nonExistentEmail = "this email doesnt exists"

        val invalidOauthId = " "
        val nonExistentOauthId = "this oauth id doesnt exists"

        // when: getting a user by a invalid email email
        when (val result = service.getUserByEmailOrAuthId(email = invalidEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.InvalidEmail,
                    EXPECT_INVALID_EMAIL + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a non existent email
        when (val result = service.getUserByEmailOrAuthId(email = nonExistentEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.UserNotFound,
                    EXPECTED_USER_NOT_FOUND + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a invalid oauth id
        when (val result = service.getUserByEmailOrAuthId(oAuthId = invalidOauthId)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.InvalidOauthId,
                    EXPECTED_INVALID_OAUTHID + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a non existent oauth id
        when (val result = service.getUserByEmailOrAuthId(oAuthId = nonExistentOauthId)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.UserNotFound,
                    EXPECTED_USER_NOT_FOUND + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a non existent email and oauth id
        when (val result = service.getUserByEmailOrAuthId(oAuthId = nonExistentOauthId, email = nonExistentEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.UserNotFound,
                    EXPECTED_USER_NOT_FOUND + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a invalid email and oauth id
        when (val result = service.getUserByEmailOrAuthId(oAuthId = invalidOauthId, email = invalidEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.InvalidOauthId,
                    EXPECTED_INVALID_OAUTHID + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: passing null in both email and oauth id
        when (val result = service.getUserByEmailOrAuthId()) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.InvalidQueryParams,
                    EXPECTED_INVALID_QUERY_PARAMS + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }
    }

    companion object {
        private val servicesUtils = ServicesUtils()

        const val CREATE_ID_ERROR = "Unexpected error occurred when creating the User. UserId should be >= 0"

        const val GET_USER_ID_ERROR = "User id should be the same as the created one"
        const val GET_USERNAME_ERROR = "User username should be the same as the created one"
        const val GET_USER_EMAIL_ERROR = "User email should be the same as the created one"
        const val GET_USER_ROLE_ERROR = "User role should be the same as the created one"
        const val GET_USER_OAUTHID_ERROR = "User oauthId should be the same as the created one"
        const val GET_USER_CREATEDAT_ERROR = "User createdAt should be the same as the created one"

        const val EXPECTED_ERROR_BUT_GOT_USER = "Expected an error, but got a user:"
        const val EXPECT_INVALID_EMAIL = "Expected an invalid email exception, but got:"
        const val EXPECTED_USER_NOT_FOUND = "Expected an user not found exception, but got:"
        const val EXPECTED_INVALID_OAUTHID = "Expected an invalid oauth id exception, but got:"
        const val EXPECTED_INVALID_QUERY_PARAMS = "Expected an invalid query params exception, but got:"
    }
}
