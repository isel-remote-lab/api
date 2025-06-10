package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.props.Role
import isel.rl.core.services.utils.UsersServicesUtils
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class UsersServicesTests {
    @Nested
    inner class LoginUser {
        @Test
        fun `login user`() {
            // given: a user service
            val clock = TestClock()
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            // when: creating a user
            UsersServicesUtils.loginUser(
                usersServices,
            )
        }
    }

    @Nested
    inner class UserRetrieval {
        @Test
        fun `get user by id`() {
            // given: a user services
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: creating a user
            val user =
                UsersServicesUtils.loginUser(
                    service,
                )

            // when: getting the user by id
            UsersServicesUtils.getUserById(
                service,
                user,
            )
        }

        @Test
        fun `get user by id (invalid id)`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: getting a user by an invalid id
            UsersServicesUtils.getUserById(
                service,
                "invalidId",
                expectedServiceException = ServicesExceptions.Users.InvalidUserId::class,
            )
        }

        @Test
        fun `get user by id (negative id)`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: getting a user by a negative id
            UsersServicesUtils.getUserById(
                service,
                "-1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `get user by id (non existent id)`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: getting a user by a non existent id
            UsersServicesUtils.getUserById(
                service,
                "999999",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `get user by email`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: creating a user
            val user =
                UsersServicesUtils.loginUser(
                    service,
                )

            // when: getting the user by email
            UsersServicesUtils.getUserByEmail(
                service,
                user,
            )
        }

        @Test
        fun `get user by email (invalid email)`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: getting a user by an invalid email
            UsersServicesUtils.getUserByEmail(
                service,
                " ",
                expectedServiceException = ServicesExceptions.Users.InvalidEmail::class,
            )
        }

        @Test
        fun `get user by email (non existent email)`() {
            // given: a user service
            val clock = TestClock()
            val service = UsersServicesUtils.createUsersServices(clock)

            // when: getting a user by a non existent email
            UsersServicesUtils.getUserByEmail(
                service,
                "this email doesnt exists",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }
    }

    @Nested
    inner class UpdateUser {
        @Test
        fun `update user role`() {
            // given: a user service
            val clock = TestClock()
            val usersServices = UsersServicesUtils.createUsersServices(clock)

            val actorUser = UsersServicesUtils.InitialUser(role = Role.ADMIN.char)

            // when: creating the actor user
            usersServices.createUser(
                role = actorUser.role,
                name = actorUser.name,
                email = actorUser.email,
            )

            // when: creating a target user
            val targetUser = UsersServicesUtils.loginUser(usersServices)

            // when: updating the target user role
            UsersServicesUtils.updateUserRole(
                usersServices,
                actorUser = actorUser,
                targetUserId = targetUser.id.toString(),
            )
        }

        @Test
        fun `update user role (invalid target user id)`() {
            // given: a user service
            val clock = TestClock()
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // when: creating the actor user
            val actorUser = UsersServicesUtils.InitialUser(role = Role.ADMIN.char)

            // when: creating the actor user
            usersService.createUser(
                role = actorUser.role,
                name = actorUser.name,
                email = actorUser.email,
            )

            // when: updating the target user role with an invalid target user id
            UsersServicesUtils.updateUserRole(
                usersService,
                actorUser = actorUser,
                targetUserId = "invalidId",
                expectedServiceException = ServicesExceptions.Users.InvalidUserId::class,
            )
        }

        @Test
        fun `update user role (target user not found)`() {
            // given: a user service
            val clock = TestClock()
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // when: creating the actor user
            val actorUser = UsersServicesUtils.InitialUser(role = Role.ADMIN.char)

            // when: creating the actor user
            usersService.createUser(
                role = actorUser.role,
                name = actorUser.name,
                email = actorUser.email,
            )

            // when: updating the target user role with a non-existent target user id
            UsersServicesUtils.updateUserRole(
                usersService,
                actorUser = actorUser,
                targetUserId = "99999999",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `update user role (null role)`() {
            // given: a user service
            val clock = TestClock()
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // when: creating the actor user
            val actorUser = UsersServicesUtils.loginUser(usersService, UsersServicesUtils.InitialUser(role = Role.ADMIN.char))

            // when: creating a target user
            val targetUser = UsersServicesUtils.loginUser(usersService)

            // when: updating the target user role with null role
            UsersServicesUtils.updateUserRole(
                usersService,
                actorUser = actorUser,
                targetUserId = targetUser.id.toString(),
                newRole = null,
                expectedServiceException = ServicesExceptions.Users.InvalidRole::class,
            )
        }

        @Test
        fun `update user role (invalid role)`() {
            // given: a user service
            val clock = TestClock()
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // when: creating the actor user
            val actorUser = UsersServicesUtils.loginUser(usersService, UsersServicesUtils.InitialUser(role = Role.ADMIN.char))

            // when: creating a target user
            val targetUser = UsersServicesUtils.loginUser(usersService)

            // when: updating the target user role with an invalid role
            val invalidRole = "invalidRole"
            UsersServicesUtils.updateUserRole(
                usersService,
                actorUser = actorUser,
                targetUserId = targetUser.id.toString(),
                newRole = invalidRole,
                expectedServiceException = ServicesExceptions.Users.InvalidRole::class,
            )
        }
    }
}
