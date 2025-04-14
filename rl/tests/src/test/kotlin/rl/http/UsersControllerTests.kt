package rl.http

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import rl.host.RemoteLabApp
import rl.http.model.Problem
import rl.http.model.user.UserOutputModel
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UsersControllerTests {
    @Test
    fun `create user test and get by Id, Email and Id`() {
        // given: a test client
        val testClient = WebTestClient.bindToServer().baseUrl(httpUtils.baseUrl).build()

        val oAuthId = httpUtils.repoUtils.newTestOauthId()
        val role = httpUtils.repoUtils.randomUserRole()
        val username = httpUtils.repoUtils.newTestUsername()
        val email = httpUtils.repoUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 201 Created
        val responseUserId = testClient
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
            .expectBody<UserOutputModel>()
            .consumeWith { result ->
                val user = result.responseBody
                assertNotNull(user)
                assertEquals(userId.toString(), user.id)
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
            .expectBody<UserOutputModel>()
            .consumeWith { result ->
                val user = result.responseBody
                assertNotNull(user)
                assertEquals(userId.toString(), user.id)
                assertEquals(oAuthId.oAuthIdInfo, user.oauthId)
                assertEquals(role.char, user.role)
                assertEquals(username.usernameInfo, user.username)
                assertEquals(email.emailInfo, user.email)
            }

        // when: doing a GET by oathid
        // then: the response is an 200 OK
        testClient
            .get()
            .uri(Uris.Users.GET_BY_OAUTHID, oAuthId.oAuthIdInfo)
            .exchange()
            .expectStatus().isOk
            .expectBody<UserOutputModel>()
            .consumeWith { result ->
                val user = result.responseBody
                assertNotNull(user)
                assertEquals(userId.toString(), user.id)
                assertEquals(oAuthId.oAuthIdInfo, user.oauthId)
                assertEquals(role.char, user.role)
                assertEquals(username.usernameInfo, user.username)
                assertEquals(email.emailInfo, user.email)
            }

    }

    @Test
    fun `create user with invalid email`() {
        // given: a test client
        val testClient = WebTestClient.bindToServer().baseUrl(httpUtils.baseUrl).build()

        val oAuthId = httpUtils.repoUtils.newTestOauthId()
        val role = httpUtils.repoUtils.randomUserRole()
        val username = httpUtils.repoUtils.newTestUsername()
        val email = ""

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
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
        val testClient = WebTestClient.bindToServer().baseUrl(httpUtils.baseUrl).build()

        val oAuthId = httpUtils.repoUtils.newTestOauthId()
        val role = "invalidRole"
        val username = httpUtils.repoUtils.newTestUsername()
        val email = httpUtils.repoUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
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
        val testClient = WebTestClient.bindToServer().baseUrl(httpUtils.baseUrl).build()

        val oAuthId = httpUtils.repoUtils.newTestOauthId()
        val role = httpUtils.repoUtils.randomUserRole()
        val username = ""
        val email = httpUtils.repoUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
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
        val testClient = WebTestClient.bindToServer().baseUrl(httpUtils.baseUrl).build()

        val oAuthId = ""
        val role = httpUtils.repoUtils.randomUserRole()
        val username = httpUtils.repoUtils.newTestUsername()
        val email = httpUtils.repoUtils.newTestEmail()

        // when: doing a POST
        // then: the response is an 400 Bad Request
        testClient
            .post()
            .uri(Uris.Users.CREATE)
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

    companion object {
        private lateinit var context: ConfigurableApplicationContext
        private val httpUtils = HttpUtils()

        @JvmStatic
        @BeforeAll
        fun start() {
            println("starting app")
            context = runApplication<RemoteLabApp>(
                "--server.port=${
                    httpUtils.port
                }"
            )
            waitForServer(InetSocketAddress(InetAddress.getLocalHost(), httpUtils.port))
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            context.stop()
        }

        private fun waitForServer(address: InetSocketAddress) {
            val socket = Socket()
            socket.connect(address, 2000)
            socket.close()
        }
    }
}