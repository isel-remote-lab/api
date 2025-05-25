package isel.rl.core.services.utils

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.repository.jdbi.transaction.JdbiTransactionManager
import isel.rl.core.services.TestClock
import isel.rl.core.services.UsersService
import isel.rl.core.services.utils.ServicesUtils.jdbi
import isel.rl.core.services.utils.ServicesUtils.usersDomain
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object UsersServicesUtils {
    /**
     * Creates a new instance of the [UsersService] with a [JdbiTransactionManager] and [UsersDomain].
     *
     * @param testClock The [TestClock] instance to be used.
     * @return A new instance of [UsersService].
     */
    fun createUsersServices(testClock: TestClock): UsersService =
        UsersService(
            JdbiTransactionManager(jdbi),
            testClock,
            usersDomain,
        )

    data class InitialUser(
        val id: Int = 0,
        val name: String = newTestUsername(),
        val email: String = newTestEmail(),
        val role: String = randomUserRole(),
        val createdAt: Instant = Instant.DISTANT_PAST,
    )

    fun loginUser(
        usersService: UsersService,
        initialUser: InitialUser = InitialUser(),
        expectedServiceException: KClass<*>? = null,
    ): InitialUser {
        val user =
            usersService.login(
                initialUser.name,
                initialUser.email,
            )

        if (expectedServiceException != null) {
            assertTrue(user is Failure, "Expected a failure, but got: $user")
            assertTrue(
                expectedServiceException.isInstance(user.value),
                "Expected a ServiceException, but got: ${user.value}",
            )
            return initialUser
        }
        assertTrue(user is Success, "Expected a successful user creation, but got: $user")
        assertEquals(
            initialUser.name,
            user.value.first.name.nameInfo,
            "Username does not match expected value.",
        )
        assertEquals(
            initialUser.email,
            user.value.first.email.emailInfo,
            "Email does not match expected value.",
        )
        return initialUser.copy(
            id = user.value.first.id,
            createdAt = user.value.first.createdAt,
            role = user.value.first.role.char,
        )
    }

    fun getUserById(
        usersService: UsersService,
        expectedUser: InitialUser,
    ) {
        val user = usersService.getUserById(expectedUser.id.toString())

        assertTrue(user is Success, "Expected a successful user retrieval, but got: $user")
        assertUser(
            expectedUser = expectedUser,
            actualUser = user.value,
        )
    }

    fun getUserById(
        usersService: UsersService,
        userId: String,
        expectedServiceException: KClass<*>,
    ) {
        val user = usersService.getUserById(userId)

        assertTrue(user is Failure, "Expected a failure, but got: $user")
        assertTrue(
            expectedServiceException.isInstance(user.value),
            "Expected a ServiceException, but got: ${user.value}",
        )
    }

    fun getUserByEmail(
        usersService: UsersService,
        expectedUser: InitialUser,
    ) {
        val user = usersService.getUserByEmail(expectedUser.email)

        assertTrue(user is Success, "Expected a successful user retrieval, but got: $user")
        assertUser(
            expectedUser = expectedUser,
            actualUser = user.value,
        )
    }

    fun getUserByEmail(
        usersService: UsersService,
        email: String,
        expectedServiceException: KClass<*>,
    ) {
        val user = usersService.getUserByEmail(email)

        assertTrue(user is Failure, "Expected a failure, but got: $user")
        assertTrue(
            expectedServiceException.isInstance(user.value),
            "Expected a ServiceException, but got: ${user.value}",
        )
    }

    fun updateUserRole(
        usersService: UsersService,
        actorUser: InitialUser,
        targetUserId: String,
        newRole: String? = randomUserRole(),
        expectedServiceException: KClass<*>? = null,
    ) {
        val user =
            usersService.updateUserRole(
                actorUserId =
                    User(
                        id = actorUser.id,
                        role = Role.entries.firstOrNull { it.char == actorUser.role } ?: Role.STUDENT,
                        name = Name(actorUser.name),
                        email = Email(actorUser.email),
                        createdAt = actorUser.createdAt,
                    ),
                targetUserId = targetUserId,
                newRole = newRole,
            )

        if (expectedServiceException != null) {
            assertTrue(user is Failure, "Expected a failure, but got: $user")
            assertTrue(
                expectedServiceException.isInstance(user.value),
                "Expected a ServiceException, but got: ${user.value}",
            )
            return
        }
        assertTrue(user is Success, "Expected a successful user role update, but got: $user")
    }

    /**
     * Generates a random username for testing purposes.
     */
    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    /**
     * Generates a random email for testing purposes.
     */
    fun newTestEmail() = "email-${abs(Random.nextLong())}"

    /**
     * Generates a random user role for testing purposes.
     */
    fun randomUserRole() = Role.entries.random().char

    fun assertUser(
        expectedUser: InitialUser,
        actualUser: User,
    ) {
        assertEquals(
            expectedUser.id,
            actualUser.id,
            "User ID does not match expected value.",
        )
        assertEquals(
            expectedUser.name,
            actualUser.name.nameInfo,
            "Username does not match expected value.",
        )
        assertEquals(
            expectedUser.email,
            actualUser.email.emailInfo,
            "Email does not match expected value.",
        )
        assertEquals(
            expectedUser.role,
            actualUser.role.char,
            "Role does not match expected value.",
        )
        assertEquals(
            expectedUser.createdAt,
            actualUser.createdAt,
            "CreatedAt does not match expected value.",
        )
    }
}
