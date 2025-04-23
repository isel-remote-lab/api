package isel.rl.core.utils

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
    classes = [RemoteLabApp::class]
)
class UsersTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `create user test and get by Id, Email and OAuthId`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: creating a user
        val (userId, initialUser) = testClient.createUser()

        // when: doing a GET by id
        testClient.getUserByIdAndVerify(userId, initialUser)

        // when: doing a GET by email
        testClient.getUserByEmailAndVerify(initialUser.email, initialUser)

        // when: doing a GET by oauthId
        testClient.getUserByOAuthIdAndVerify(initialUser.oAuthId, initialUser)
    }

    @Test
    fun `create user with invalid email`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser(
            email = ""
        )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidEmail)
    }

    @Test
    fun `create user with invalid role`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser(
            role = "invalid-role"
        )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidRole)
    }

    @Test
    fun `create user with invalid username`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser(
            username = ""
        )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidUsername)
    }

    @Test
    fun `create user with invalid oauthId`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser(
            oAuthId = ""
        )

        // when: doing a POST
        testClient.createInvalidUser(initialUser, Problem.invalidOauthId)
    }

    @Test
    fun `create user without api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .bodyValue(
                mapOf(
                    "oauthId" to initialUser.oAuthId,
                    "role" to initialUser.role,
                    "username" to initialUser.username,
                    "email" to initialUser.email
                ),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `create user with invalid api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val initialUser = InitialUser()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, "invalid-api-key")
            .bodyValue(
                mapOf(
                    "oauthId" to initialUser.oAuthId,
                    "role" to initialUser.role,
                    "username" to initialUser.username,
                    "email" to initialUser.email
                ),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `get user by oAuthId with invalid api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()

        // when: doing a GET
        // then: the response is an 403 Forbidden
        testClient
            .get()
            .uri { builder ->
                builder
                    .path(Uris.Users.GET_BY_OAUTHID)
                    .queryParam("oauthid", oAuthId)
                    .build()
            }
            .header(httpUtils.apiHeader, "invalid-api-key")
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
            val email: String = httpUtils.newTestEmail()
        )

        private fun WebTestClient.createUser(): Pair<Int, InitialUser> {
            val user = InitialUser()

            val responseUserId = post()
                .uri(Uris.Users.CREATE)
                .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
                .bodyValue(
                    mapOf(
                        "oauthId" to user.oAuthId,
                        "role" to user.role,
                        "username" to user.username,
                        "email" to user.email
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

        private fun WebTestClient.createInvalidUser(initialUser: InitialUser, expectedProblem: Problem) {
            post()
                .uri(Uris.Users.CREATE)
                .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
                .bodyValue(
                    mapOf(
                        "oauthId" to initialUser.oAuthId,
                        "role" to initialUser.role,
                        "username" to initialUser.username,
                        "email" to initialUser.email
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

        private fun WebTestClient.getUserByIdAndVerify(userId: Int, expectedUser: InitialUser) {
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

        private fun WebTestClient.getUserByEmailAndVerify(userEmail: String, expectedUser: InitialUser) {
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

        private fun WebTestClient.getUserByOAuthIdAndVerify(oAuthId: String, expectedUser: InitialUser) {
            get()
                .uri { builder ->
                    builder
                        .path(Uris.Users.GET_BY_OAUTHID)
                        .queryParam("oauthid", oAuthId)
                        .build()
                }
                .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
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
    }
}