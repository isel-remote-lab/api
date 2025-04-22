package isel.rl.core.utils

import isel.rl.core.domain.Uris
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.user.UserOutputModel
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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

        val oAuthId = httpUtils.newTestOauthId()
        val role = httpUtils.randomUserRole()
        val username = httpUtils.newTestUsername()
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 201 Created
        val responseUserId = testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role.char,
                    "username" to username.usernameInfo,
                    "email" to email.emailInfo
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<Int>()
            .returnResult()

        val userId = responseUserId.responseBody!!
        assertTrue(userId >= 0)

        // when: doing a GET by id
        // then: the response is an 200 OK
        testClient
            .get()
            .uri(Uris.Users.GET, userId)
            .exchange()
            .expectStatus().isOk
            .expectBody<Map<String, UserOutputModel>>()
            .consumeWith { result ->
                assertNotNull(result)
                val user = result.responseBody?.get(USER_OUTPUT_MAP_KEY)
                assertNotNull(user)
                assertEquals(userId, user.id)
                assertEquals(oAuthId.oAuthIdInfo, user.oauthId)
                assertEquals(role.char, user.role)
                assertEquals(username.usernameInfo, user.username)
                assertEquals(email.emailInfo, user.email)
            }

        // when: doing a GET by email
        // then: the response is an 200 OK
        testClient
            .get()
            .uri { builder ->
                builder
                    .path(Uris.Users.GET_BY_EMAIL)
                    .queryParam("email", email.emailInfo)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody<Map<String, UserOutputModel>>()
            .consumeWith { result ->
                assertNotNull(result)
                val user = result.responseBody?.get(USER_OUTPUT_MAP_KEY)
                assertNotNull(user)
                assertEquals(userId, user.id)
                assertEquals(oAuthId.oAuthIdInfo, user.oauthId)
                assertEquals(role.char, user.role)
                assertEquals(username.usernameInfo, user.username)
                assertEquals(email.emailInfo, user.email)
            }

        // when: doing a GET by oauthId
        // then: the response is an 200 OK
        testClient
            .get()
            .uri { builder ->
                builder
                    .path(Uris.Users.GET_BY_OAUTHID)
                    .queryParam("oauthid", oAuthId.oAuthIdInfo)
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
                assertEquals(userId, user.id)
                assertEquals(oAuthId.oAuthIdInfo, user.oauthId)
                assertEquals(role.char, user.role)
                assertEquals(username.usernameInfo, user.username)
                assertEquals(email.emailInfo, user.email)
            }
    }

    @Test
    fun `create user with invalid email`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()
        val role = httpUtils.randomUserRole()
        val username = httpUtils.newTestUsername()
        val email = ""

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role.char,
                    "username" to username.usernameInfo,
                    "email" to email
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<Problem>()
            .consumeWith { result ->
                val problem = result.responseBody
                assertNotNull(problem)
                assertEquals(Problem.invalidEmail.type, problem.type)
                assertEquals(Problem.invalidEmail.title, problem.title)
                assertEquals(Problem.invalidEmail.details, problem.details)
            }
    }

    @Test
    fun `create user with invalid role`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()
        val role = "invalidRole"
        val username = httpUtils.newTestUsername()
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role,
                    "username" to username.usernameInfo,
                    "email" to email.emailInfo
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<Problem>()
            .consumeWith { result ->
                val problem = result.responseBody
                assertNotNull(problem)
                assertEquals(Problem.invalidRole.type, problem.type)
                assertEquals(Problem.invalidRole.title, problem.title)
                assertEquals(Problem.invalidRole.details, problem.details)
            }
    }

    @Test
    fun `create user with invalid username`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()
        val role = httpUtils.randomUserRole()
        val username = ""
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role.char,
                    "username" to username,
                    "email" to email.emailInfo
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<Problem>()
            .consumeWith { result ->
                val problem = result.responseBody
                assertNotNull(problem)
                assertEquals(Problem.invalidUsername.type, problem.type)
                assertEquals(Problem.invalidUsername.title, problem.title)
                assertEquals(Problem.invalidUsername.details, problem.details)
            }
    }

    @Test
    fun `create user with invalid oauthId`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = ""
        val role = httpUtils.randomUserRole()
        val username = httpUtils.newTestUsername()
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, httpUtils.apiKey.apiKeyInfo)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId,
                    "role" to role.char,
                    "username" to username.usernameInfo,
                    "email" to email.emailInfo
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody<Problem>()
            .consumeWith { result ->
                val problem = result.responseBody
                assertNotNull(problem)
                assertEquals(Problem.invalidOauthId.type, problem.type)
                assertEquals(Problem.invalidOauthId.title, problem.title)
                assertEquals(Problem.invalidOauthId.details, problem.details)
            }
    }

    @Test
    fun `create user without api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()
        val role = httpUtils.randomUserRole()
        val username = httpUtils.newTestUsername()
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role.char,
                    "username" to username.usernameInfo,
                    "email" to email.emailInfo
                ),
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `create user with invalid api key`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        val oAuthId = httpUtils.newTestOauthId()
        val role = httpUtils.randomUserRole()
        val username = httpUtils.newTestUsername()
        val email = httpUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 403 Forbidden
        testClient
            .post()
            .uri(Uris.Users.CREATE)
            .header(httpUtils.apiHeader, "invalid-api-key")
            .bodyValue(
                mapOf(
                    "oauthId" to oAuthId.oAuthIdInfo,
                    "role" to role.char,
                    "username" to username.usernameInfo,
                    "email" to email.emailInfo
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
                    .queryParam("oauthid", oAuthId.oAuthIdInfo)
                    .build()
            }
            .header(httpUtils.apiHeader, "invalid-api-key")
            .exchange()
            .expectStatus().isForbidden
    }

    companion object {
        private val httpUtils = HttpUtils()
        const val USER_OUTPUT_MAP_KEY = "user"
    }
}