package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.host.LaboratoriesTests.Companion.InitialLaboratory
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.http.model.SuccessResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.Test

/**
 * This class contains tests for the authentication system of the application.
 * It tests the creation of laboratories using both authToken and cookies.
 * It uses the laboratories creation endpoint to test the authentication system. Every controller that has the
 * authenticatedUser parameter uses automatically the authentication system.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [RemoteLabApp::class],
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class AuthTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `create lab with authToken`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: creating a lab with authToken
        testClient.createLabWithAuthToken()
    }

    @Test
    fun `create lab with cookie`() {
        // given: a test client
        val testClient = httpUtils.buildTestClient(port)

        // when: creating a lab with cookie
        testClient.createLabWithCookie()
    }

    companion object {
        private val httpUtils = HttpUtils()

        private fun WebTestClient.createLabWithAuthToken() {
            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(this)

            val initialLaboratory = InitialLaboratory()

            this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .bodyValue(
                    initialLaboratory.mapOf(),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<SuccessResponse>()
        }

        private fun WebTestClient.createLabWithCookie() {
            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(this)

            val initialLaboratory = InitialLaboratory()

            this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .cookie("token", authToken)
                .bodyValue(
                    initialLaboratory.mapOf(),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<SuccessResponse>()
        }
    }
}
