package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.utils.AuthToken
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.HttpUtilsTest
import isel.rl.core.host.utils.UserId
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [RemoteLabApp::class],
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class UsersTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `login user test`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        // when: logging a user that does not exist
        val user = usersHelper.createTestUser(testClient)

        // then: the user is created
        usersHelper.getUserById(testClient, user)
    }

    @Test
    fun `login already created user`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        // when: logging a user that does not exist
        val user = usersHelper.createTestUser(testClient)

        // then: the user is created
        usersHelper.getUserById(testClient, user)

        // when: logging the same user again
        val userSecondLogin = usersHelper.createTestUser(testClient, user)

        // then: the user is the same
        assertEquals(user.id, userSecondLogin.id)
    }

    @Test
    fun `create user test and get by Id and Email`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        // when: creating a user
        val user = usersHelper.createTestUser(testClient)

        // when: doing a GET by id
        usersHelper.getUserById(testClient, user)

        // when: doing a GET by email
        usersHelper.getUserByEmail(testClient, user)
    }

    @Test
    fun `create user with invalid email`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        val initialUser =
            HttpUtilsTest.Users.InitialUser(
                email = Email(""),
            )

        // when: doing a POST
        usersHelper.createInvalidUser(testClient, initialUser, Problem.invalidEmail)
    }

    @Test
    fun `create user with invalid username`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        val initialUser =
            HttpUtilsTest.Users.InitialUser(
                name = Name(""),
            )

        // when: doing a POST
        usersHelper.createInvalidUser(testClient, initialUser, Problem.invalidName)
    }

    @Test
    fun `create user without api key`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        val initialUser =
            HttpUtilsTest.Users.InitialUser()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Auth.LOGIN)
            .bodyValue(
                HttpUtilsTest.Users.InitialUser.createBodyValue(initialUser),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `create user with invalid api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUserLogin()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Auth.LOGIN)
            .header(httpUtils.apiHeader, "invalid-api-key")
            .bodyValue(
                initialUser.mapOf(),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `update user role`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        // when: creating the actor user directly in the DB to be Admin
        val actorUserInitialInfo = HttpUtilsTest.Users.InitialUser()

        HttpUtilsTest.Users.createDBUser(
            actorUserInitialInfo.name.nameInfo,
            actorUserInitialInfo.email.emailInfo,
            Role.ADMIN.char,
        )

        // when: logging the actor user
        val actorUser = usersHelper.createTestUser(testClient, actorUserInitialInfo)

        // when: creating the target user
        val targetUser = usersHelper.createTestUser(testClient)

        // when: updating the user role
        val newRole = usersHelper.randomUserRole()
        testClient
            .patch()
            .uri(Uris.Users.UPDATE_USER_ROLE, targetUser.id)
            .header(httpUtils.authHeader, "Bearer ${actorUser.authToken}")
            .bodyValue(
                mapOf(
                    ROLE_PROP to newRole.char,
                ),
            )
            .exchange()
            .expectStatus().isOk

        // then: the user role is updated
        usersHelper.getUserById(testClient, targetUser.copy(role = newRole))
    }

    @Test
    fun `update user role (not enough permission)`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        // when: creating the actor user directly in the DB to be User
        val actorUserInitialInfo = HttpUtilsTest.Users.InitialUser()

        HttpUtilsTest.Users.createDBUser(
            actorUserInitialInfo.name.nameInfo,
            actorUserInitialInfo.email.emailInfo,
            Role.STUDENT.char,
        )

        // when: logging the actor user
        val actorUser = usersHelper.createTestUser(testClient, actorUserInitialInfo)

        // when: creating the target user
        val targetUser = usersHelper.createTestUser(testClient)

        // when: updating the user role
        val newRole = usersHelper.randomUserRole()
        testClient
            .patch()
            .uri(Uris.Users.UPDATE_USER_ROLE, targetUser.id)
            .header(httpUtils.authHeader, "Bearer ${actorUser.authToken}")
            .bodyValue(
                mapOf(
                    ROLE_PROP to newRole.char,
                ),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    companion object {
        private val httpUtils = HttpUtils()
        private val usersHelper = HttpUtilsTest.Users
        const val USER_OUTPUT_MAP_KEY = "user"
        private const val ID_PROP = "id"
        private const val ROLE_PROP = "role"
        private const val NAME_PROP = "name"
        private const val EMAIL_PROP = "email"

        private data class InitialUser(
            val role: String = httpUtils.randomUserRole(),
            val name: String = httpUtils.newTestUsername(),
            val email: String = httpUtils.newTestEmail(),
        )

        private data class InitialUserLogin(
            val role: String = "S",
            val name: String = httpUtils.newTestUsername(),
            val email: String = httpUtils.newTestEmail(),
        ) {
            fun mapOf() =
                mapOf(
                    NAME_PROP to name,
                    EMAIL_PROP to email,
                )
        }

        private fun WebTestClient.createInvalidUser(
            initialUser: InitialUserLogin,
            expectedProblem: Problem,
        ) {
            post()
                .uri(Uris.Auth.LOGIN)
                .header(httpUtils.apiHeader, httpUtils.apiKey)
                .bodyValue(
                    initialUser.mapOf(),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { result ->
                    val problem = result.responseBody
                    assertNotNull(problem)
                    assertEquals(expectedProblem.type, problem.type)
                    assertEquals(expectedProblem.title, problem.title)
                    assertEquals(expectedProblem.detail, problem.detail)
                }
        }

        private fun WebTestClient.getUserByIdAndVerify(
            userId: Int,
            expectedUser: InitialUser,
        ) {
            get()
                .uri(Uris.Users.GET, userId)
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val response = result.responseBody
                    val responseBody = (response?.data as Map<*, *>)[USER_OUTPUT_MAP_KEY] as Map<*, *>
                    assertNotNull(response)
                    assertEquals("User found with the id $userId", response.message)
                    assertEquals(userId, responseBody[ID_PROP])
                    assertEquals(expectedUser.role, responseBody[ROLE_PROP])
                    assertEquals(expectedUser.name, responseBody[NAME_PROP])
                    assertEquals(expectedUser.email, responseBody[EMAIL_PROP])
                }
        }

        private fun WebTestClient.getUserByEmailAndVerify(
            userEmail: String,
            expectedUser: InitialUser,
        ) {
            get()
                .uri { builder ->
                    builder
                        .path(Uris.Users.GET_BY_EMAIL)
                        .queryParam("email", userEmail)
                        .build()
                }
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val response = result.responseBody
                    val responseBody = (response?.data as Map<*, *>)[USER_OUTPUT_MAP_KEY] as Map<*, *>
                    assertNotNull(response)
                    assertEquals("User found with the email $userEmail", response.message)
                    assertEquals(expectedUser.role, responseBody[ROLE_PROP])
                    assertEquals(expectedUser.name, responseBody[NAME_PROP])
                    assertEquals(expectedUser.email, responseBody[EMAIL_PROP])
                }
        }

        private fun WebTestClient.loginUser(initialUser: InitialUserLogin): Pair<Int, InitialUser> {
            var ret: Pair<Int, InitialUser>? = null

            post()
                .uri(Uris.Auth.LOGIN)
                .header(httpUtils.apiHeader, httpUtils.apiKey)
                .bodyValue(
                    initialUser.mapOf(),
                )
                .exchange()
                .expectStatus().isOk
                .expectCookie().exists("token")
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    val cookies = result.responseCookies["token"]
                    assertNotNull(cookies)
                    val cookie = cookies[0].value
                    assertNotNull(result)
                    val actualMessage = result.responseBody
                    assertNotNull(actualMessage)
                    val responseBodyUser = (actualMessage.data as Map<*, *>)[USER_OUTPUT_MAP_KEY] as Map<*, *>
                    assertEquals("User logged in successfully", actualMessage.message)
                    assertEquals(initialUser.role, responseBodyUser[ROLE_PROP])
                    assertEquals(initialUser.name, responseBodyUser[NAME_PROP])
                    assertEquals(initialUser.email, responseBodyUser[EMAIL_PROP])
                    ret = responseBodyUser[ID_PROP] as Int to
                        InitialUser(
                            role = responseBodyUser[ROLE_PROP] as String,
                            name = responseBodyUser[NAME_PROP] as String,
                            email = responseBodyUser[EMAIL_PROP] as String,
                        )

                    val token = (actualMessage.data as Map<*, *>)["token"] as String
                    assertTrue(token.isNotBlank())
                    assertEquals(cookie, token)
                }

            assertNotNull(ret)
            return ret!!
        }

        private fun WebTestClient.loginExistingUser(
            username: String,
            email: String,
        ): Pair<UserId, AuthToken> {
            var userId: UserId = -1
            var token: AuthToken = ""
            // when: doing a POST
            // then: the response is an 201 Created
            post()
                .uri(Uris.Auth.LOGIN)
                .header(httpUtils.apiHeader, httpUtils.apiKey)
                .bodyValue(
                    mapOf(
                        "name" to username,
                        "email" to email,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val actualMessage = result.responseBody
                    assertNotNull(actualMessage)
                    val responseBodyUser = (actualMessage.data as Map<*, *>)[USER_OUTPUT_MAP_KEY] as Map<*, *>
                    assertEquals("User logged in successfully", actualMessage.message)
                    userId = responseBodyUser["id"] as Int
                    token = (actualMessage.data as Map<*, *>)["token"] as String
                }

            assertNotNull(userId)
            assertTrue(token.isNotBlank())

            return userId to token
        }
    }
}
