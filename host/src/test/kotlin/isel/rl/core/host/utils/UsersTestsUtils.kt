package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.utils.HttpUtils.API_HEADER_NAME
import isel.rl.core.host.utils.HttpUtils.API_KEY_TEST
import isel.rl.core.host.utils.HttpUtils.AUTH_COOKIE_NAME
import isel.rl.core.host.utils.HttpUtils.AUTH_HEADER_NAME
import isel.rl.core.host.utils.HttpUtils.assertProblem
import isel.rl.core.host.utils.HttpUtils.getBodyDataFromResponse
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import kotlinx.datetime.Instant
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

object UsersTestsUtils {
    private const val USER_OUTPUT_MAP_KEY = "user"
    private const val TOKEN_OUTPUT_MAP_KEY = "token"
    private const val ID_PROP = "id"
    private const val ROLE_PROP = "role"
    private const val NAME_PROP = "name"
    private const val EMAIL_PROP = "email"
    private const val CREATED_AT_PROP = "createdAt"

    data class InitialUser(
        val id: Int = 0,
        val role: Role = randomUserRole(),
        val name: Name = newTestUsername(),
        val email: Email = newTestEmail(),
        val createdAt: Instant = Instant.DISTANT_PAST,
        val authToken: String = "",
    ) {
        companion object {
            fun createBodyValue(initialUser: InitialUser) =
                mapOf(
                    "name" to initialUser.name.nameInfo,
                    "email" to initialUser.email.emailInfo,
                )
        }
    }

    fun newTestUsername() = Name("user-${abs(Random.nextLong())}")

    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")

    fun randomUserRole() = Role.entries.random()

    fun createTestUser(
        testClient: WebTestClient,
        initialUser: InitialUser = InitialUser(),
    ): InitialUser {
        val response =
            testClient.post()
                .uri(Uris.Auth.LOGIN)
                .header(API_HEADER_NAME, API_KEY_TEST)
                .bodyValue(InitialUser.createBodyValue(initialUser))
                .exchange()
                .expectStatus().isOk
                .expectCookie().exists(AUTH_COOKIE_NAME)
                .expectBody<SuccessResponse>()
                .returnResult()

        val data = getBodyDataFromResponse<Map<*, *>>(response, "User logged in successfully")
        val user = data[USER_OUTPUT_MAP_KEY] as Map<*, *>
        val token = data[TOKEN_OUTPUT_MAP_KEY] as String

        assertEquals(initialUser.name.nameInfo, user[NAME_PROP], "User name mismatch")
        assertEquals(initialUser.email.emailInfo, user[EMAIL_PROP], "User email mismatch")

        return initialUser.copy(
            id = user[ID_PROP] as Int,
            role = Role.entries.firstOrNull { it.char == user[ROLE_PROP] as String }!!,
            createdAt = Instant.parse(user[CREATED_AT_PROP] as String),
            authToken = token,
        )
    }

    fun createInvalidUser(
        testClient: WebTestClient,
        initialUser: InitialUser,
        expectedProblem: Problem,
    ) {
        testClient
            .post()
            .uri(Uris.Auth.LOGIN)
            .header(API_HEADER_NAME, API_KEY_TEST)
            .bodyValue(
                InitialUser.createBodyValue(initialUser),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<Problem>()
            .consumeWith { assertProblem(expectedProblem, it) }
    }

    fun getUserById(
        testClient: WebTestClient,
        expectedUser: InitialUser,
    ) {
        val response =
            testClient.get()
                .uri(Uris.Users.GET, expectedUser.id)
                .header(AUTH_HEADER_NAME, "Bearer ${expectedUser.authToken}")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        val user = getBodyDataFromResponse<Map<*, *>>(response, "User found with the id ${expectedUser.id}")
        assertUser(expectedUser, user)
    }

    fun getUserByEmail(
        testClient: WebTestClient,
        expectedUser: InitialUser,
    ) {
        val response =
            testClient.get()
                .uri { builder ->
                    builder
                        .path(Uris.Users.GET_BY_EMAIL)
                        .queryParam("email", expectedUser.email.emailInfo)
                        .build()
                }
                .header(AUTH_HEADER_NAME, "Bearer ${expectedUser.authToken}")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        val user =
            getBodyDataFromResponse<Map<*, *>>(response, "User found with the email ${expectedUser.email.emailInfo}")

        assertUser(expectedUser, user)
    }

    fun createDBUser(
        username: String,
        email: String,
        role: String,
    ): Int {
        var userId: Int = -1
        HttpUtils.runWithHandle { handle ->
            val userRepo = JdbiUsersRepository(handle)

            userId =
                userRepo.createUser(
                    HttpUtils.usersDomain.validateCreateUser(
                        role,
                        username,
                        email,
                        TestClock().now(),
                    ),
                )
        }
        assertTrue(userId > 0)
        return userId
    }

    fun assertUser(
        expectedUser: InitialUser,
        actualUser: Map<*, *>,
    ) {
        assertEquals(expectedUser.id, actualUser[ID_PROP], "User id mismatch")
        assertEquals(expectedUser.name.nameInfo, actualUser[NAME_PROP], "User name mismatch")
        assertEquals(expectedUser.email.emailInfo, actualUser[EMAIL_PROP], "User email mismatch")
        assertEquals(expectedUser.role.char, actualUser[ROLE_PROP], "User role mismatch")
        assertEquals(
            expectedUser.createdAt.epochSeconds.days,
            Instant.parse(actualUser[CREATED_AT_PROP] as String).epochSeconds.days,
            "User createdAt mismatch",
        )
    }
}
