package isel.rl.core.utils

import com.auth0.jwt.interfaces.DecodedJWT
import isel.rl.core.domain.Uris
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.user.UserOutputModel
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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
class UsersTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `login user test`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: logging a user that does not exist
        val (userId, expectedUser) = testClient.loginUser(InitialUserLogin())

        // then: the user is created
        testClient.getUserByIdAndVerify(userId, expectedUser)
    }

    @Test
    fun `login already created user`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: logging a user that does not exist
        val initialUser = InitialUserLogin()
        val (userId, expectedUser) = testClient.loginUser(initialUser)

        // then: the user is created
        testClient.getUserByIdAndVerify(userId, expectedUser)

        // when: logging the same user again
        val (actualUserId, _) = testClient.loginUser(initialUser)

        // then: the user is the same
        assertEquals(userId, actualUserId)
    }

    @Test
    fun `create user test and get by Id and Email`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: creating a user
        val (userId, expectedUser) = testClient.loginUser(InitialUserLogin())

        // when: doing a GET by id
        testClient.getUserByIdAndVerify(userId, expectedUser)

        // when: doing a GET by email
        testClient.getUserByEmailAndVerify(expectedUser.email, expectedUser)

        // when: doing a GET by oauthId
        // testClient.getUserByOAuthIdAndVerify(initialUser.oAuthId, initialUser)
    }

    @Test
    fun `create user with invalid email`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser =
            InitialUserLogin(
                email = "",
            )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidEmail)
    }

    @Test
    fun `create user with invalid username`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser =
            InitialUserLogin(
                username = "",
            )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidUsername)
    }

    @Test
    fun `create user with invalid oauthId`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser =
            InitialUserLogin(
                oAuthId = "",
            )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidOauthId)
    }

    @Test
    fun `create user without api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUserLogin()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Users.LOGIN)
            .bodyValue(
                mapOf(
                    "oauthId" to initialUser.oAuthId,
                    "username" to initialUser.username,
                    "email" to initialUser.email,
                    "accessToken" to initialUser.accessToken,
                ),
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
            .uri(Uris.Users.LOGIN)
            .header(httpUtils.apiHeader, "invalid-api-key")
            .bodyValue(
                mapOf(
                    "oauthId" to initialUser.oAuthId,
                    "username" to initialUser.username,
                    "email" to initialUser.email,
                    "accessToken" to initialUser.accessToken,
                ),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    companion object {
        private val httpUtils = HttpUtils()
        private const val USER_OUTPUT_MAP_KEY = "user"

        private data class InitialUser(
            val oAuthId: String = httpUtils.newTestOauthId(),
            val role: String = httpUtils.randomUserRole(),
            val username: String = httpUtils.newTestUsername(),
            val email: String = httpUtils.newTestEmail(),
        )

        private data class InitialUserLogin(
            val oAuthId: String = httpUtils.newTestOauthId(),
            val role: String = "S",
            val username: String = httpUtils.newTestUsername(),
            val email: String = httpUtils.newTestEmail(),
            val accessToken: String = httpUtils.newTestAccessToken(),
        )

        private fun WebTestClient.createUser(): Pair<Int, InitialUser> {
            val user = InitialUser()

            val responseUserId =
                post()
                    .uri(Uris.Users.CREATE)
                    .header(httpUtils.apiHeader, httpUtils.apiKey)
                    .bodyValue(
                        mapOf(
                            "oauthId" to user.oAuthId,
                            "role" to user.role,
                            "username" to user.username,
                            "email" to user.email,
                        ),
                    )
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<Int>()
                    .returnResult()

            val userId = responseUserId.responseBody!!
            assertTrue(userId >= 0)
            return Pair(userId, user)
        }

        private fun WebTestClient.createInvalidUser(
            initialUser: InitialUserLogin,
            expectedProblem: Problem,
        ) {
            post()
                .uri(Uris.Users.LOGIN)
                .header(httpUtils.apiHeader, httpUtils.apiKey)
                .bodyValue(
                    mapOf(
                        "oauthId" to initialUser.oAuthId,
                        "username" to initialUser.username,
                        "email" to initialUser.email,
                        "accessToken" to initialUser.accessToken,
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { result ->
                    val problem = result.responseBody
                    assertNotNull(problem)
                    assertEquals(expectedProblem.type, problem.type)
                    assertEquals(expectedProblem.title, problem.title)
                    assertEquals(expectedProblem.details, problem.details)
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
                .expectBody<Map<String, UserOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val user = result.responseBody?.get(USER_OUTPUT_MAP_KEY)
                    assertNotNull(user)
                    assertEquals(userId, user.id)
                    assertEquals(expectedUser.oAuthId, user.oauthId)
                    assertEquals(expectedUser.role, user.role)
                    assertEquals(expectedUser.username, user.username)
                    assertEquals(expectedUser.email, user.email)
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
                .expectBody<Map<String, UserOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val user = result.responseBody?.get(USER_OUTPUT_MAP_KEY)
                    assertNotNull(user)
                    assertEquals(expectedUser.oAuthId, user.oauthId)
                    assertEquals(expectedUser.role, user.role)
                    assertEquals(expectedUser.username, user.username)
                    assertEquals(expectedUser.email, user.email)
                }
        }

        private fun WebTestClient.getUserByOAuthIdAndVerify(
            oAuthId: String,
            expectedUser: InitialUser,
        ) {
            get()
                .uri { builder ->
                    builder
                        .path(Uris.Users.GET_BY_OAUTHID)
                        .queryParam("oauthid", oAuthId)
                        .build()
                }
                .header(httpUtils.apiHeader, httpUtils.apiKey)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, UserOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val user = result.responseBody?.get(USER_OUTPUT_MAP_KEY)
                    assertNotNull(user)
                    assertEquals(expectedUser.oAuthId, user.oauthId)
                    assertEquals(expectedUser.role, user.role)
                    assertEquals(expectedUser.username, user.username)
                    assertEquals(expectedUser.email, user.email)
                }
        }

        private fun WebTestClient.loginUser(initialUser: InitialUserLogin): Pair<Int, InitialUser> {
            val res =
                post()
                    .uri(Uris.Users.LOGIN)
                    .header(httpUtils.apiHeader, httpUtils.apiKey)
                    .bodyValue(
                        mapOf(
                            "oauthId" to initialUser.oAuthId,
                            "username" to initialUser.username,
                            "email" to initialUser.email,
                            "accessToken" to initialUser.accessToken,
                        ),
                    )
                    .exchange()
                    .expectStatus().isOk
                    .expectCookie().exists(httpUtils.sessionCookie)
                    .expectBody<Unit>()
                    .returnResult()

            val cookie = res.responseCookies[httpUtils.sessionCookie]?.first()?.value

            val jwt: DecodedJWT = httpUtils.validateJWTToken(cookie)

            val userId = jwt.getClaim("userId").asString()
            assertNotNull(userId, "User ID should not be null")
            return (
                userId.toInt() to
                    InitialUser(
                        oAuthId = initialUser.oAuthId,
                        role = initialUser.role,
                        username = initialUser.username,
                        email = initialUser.email,
                    )
            )
        }
    }
}
