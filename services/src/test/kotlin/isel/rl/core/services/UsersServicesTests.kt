package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.props.Role
import isel.rl.core.utils.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class UsersServicesTests {
    @Test
    fun `create user and get by id and email`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: creating a user
        val username = servicesUtils.newTestUsername()
        val email = servicesUtils.newTestEmail()
        val userRole = servicesUtils.randomUserRole()
        val createdUserResult =
            service.createUser(
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
                assertEquals(user.name.nameInfo, username, GET_USERNAME_ERROR)
                assertEquals(user.email.emailInfo, email, GET_USER_EMAIL_ERROR)
                assertEquals(user.role.char, userRole, GET_USER_ROLE_ERROR)
                assertEquals(user.createdAt, clock.now(), GET_USER_CREATED_AT_ERROR)
            }
        }

        // when: getting the user by email
        // then: Verify if the user was retrieved by email
        when (val userByEmailResult = service.getUserByEmail(email)) {
            is Either.Left -> fail("User retrieval by email failed: ${userByEmailResult.value}")
            is Either.Right -> {
                val user = userByEmailResult.value
                assertEquals(user.id, userId, GET_USER_ID_ERROR)
                assertEquals(user.name.nameInfo, username, GET_USERNAME_ERROR)
                assertEquals(user.email.emailInfo, email, GET_USER_EMAIL_ERROR)
                assertEquals(user.role.char, userRole, GET_USER_ROLE_ERROR)
                assertEquals(user.createdAt, clock.now(), GET_USER_CREATED_AT_ERROR)
            }
        }
    }

    @Test
    fun `update user role`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: creating the actor user
        val actorCreateUser = CreateUserInfo(userRole = Role.ADMIN.char)
        val actorUser = service.createUser(actorCreateUser)

        // when: creating a target user
        val targetUser = service.createUser(CreateUserInfo())

        // when: updating the target user role
        val newRole = servicesUtils.randomUserRole()
        val updateResult =
            service.updateUserRole(
                actorUserId = actorUser,
                targetUserId = targetUser.id.toString(),
                newRole = newRole,
            )

        // then: Verify if the user role was updated
        when (updateResult) {
            is Either.Left -> fail("User role update failed: ${updateResult.value}")
            is Either.Right -> assertTrue(updateResult.value)
        }
    }

    @Test
    fun `update user role (not enough permissions)`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: creating the actor user
        val actorCreateUser = CreateUserInfo(userRole = Role.STUDENT.char)
        val actorUser = service.createUser(actorCreateUser)

        // when: creating a target user
        val targetUser = service.createUser(CreateUserInfo())

        // when: updating the target user role
        val newRole = servicesUtils.randomUserRole()
        val updateResult =
            service.updateUserRole(
                actorUserId = actorUser,
                targetUserId = targetUser.id.toString(),
                newRole = newRole,
            )

        // then: Verify if the user role update failed due to insufficient permissions
        when (updateResult) {
            is Either.Left -> assertTrue(updateResult.value is ServicesExceptions.Forbidden)
            is Either.Right -> fail("Expected insufficient permissions error, but got success")
        }
    }

    @Test
    fun `update user role (null role)`() {
        // given: a user service
        val clock = TestClock()
        val service = servicesUtils.createUsersServices(clock)

        // when: creating the actor user
        val actorCreateUser = CreateUserInfo(userRole = Role.ADMIN.char)
        val actorUser = service.createUser(actorCreateUser)

        // when: creating a target user
        val targetUser = service.createUser(CreateUserInfo())

        // when: updating the target user role with null role
        val updateResult =
            service.updateUserRole(
                actorUserId = actorUser,
                targetUserId = targetUser.id.toString(),
                newRole = null,
            )

        // then: Verify if the user role update failed due to invalid role
        when (updateResult) {
            is Either.Left -> assertTrue(updateResult.value is ServicesExceptions.Users.InvalidRole)
            is Either.Right -> fail("Expected invalid role error, but got success")
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
        when (val result = service.getUserByEmail(invalidEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.InvalidEmail,
                    EXPECT_INVALID_EMAIL + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }

        // when: getting a user by a non existent email
        when (val result = service.getUserByEmail(nonExistentEmail)) {
            is Either.Left ->
                assertTrue(
                    result.value is ServicesExceptions.Users.UserNotFound,
                    EXPECTED_USER_NOT_FOUND + result.value,
                )

            is Either.Right -> fail(EXPECTED_ERROR_BUT_GOT_USER + result.value)
        }
    }

    companion object {
        private val servicesUtils = ServicesUtils()

        data class CreateUserInfo(
            val userRole: String = servicesUtils.randomUserRole(),
            val username: String = servicesUtils.newTestUsername(),
            val email: String = servicesUtils.newTestEmail(),
        )

        fun UsersService.createUser(createUser: CreateUserInfo): User {
            val user =
                createUser(
                    createUser.userRole,
                    createUser.username,
                    createUser.email,
                )

            // then: Verify if the user was created
            when (user) {
                is Either.Left -> fail("User creation failed: ${user.value}")
                is Either.Right ->
                    assertTrue(
                        user.value.id >= 0,
                        CREATE_ID_ERROR,
                    )
            }

            return user.value
        }

        const val CREATE_ID_ERROR = "Unexpected error occurred when creating the User. UserId should be >= 0"

        const val GET_USER_ID_ERROR = "User id should be the same as the created one"
        const val GET_USERNAME_ERROR = "User username should be the same as the created one"
        const val GET_USER_EMAIL_ERROR = "User email should be the same as the created one"
        const val GET_USER_ROLE_ERROR = "User role should be the same as the created one"
        const val GET_USER_CREATED_AT_ERROR = "User createdAt should be the same as the created one"

        const val EXPECTED_ERROR_BUT_GOT_USER = "Expected an error, but got a user:"
        const val EXPECT_INVALID_EMAIL = "Expected an invalid email exception, but got:"
        const val EXPECTED_USER_NOT_FOUND = "Expected an user not found exception, but got:"
    }
}
