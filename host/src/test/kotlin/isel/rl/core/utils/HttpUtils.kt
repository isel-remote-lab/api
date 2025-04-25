package isel.rl.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import isel.rl.core.domain.Uris
import isel.rl.core.domain.group.GroupDescription
import isel.rl.core.domain.group.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.DurationUnit

class HttpUtils {
    private val remoteLab = RemoteLabApp()
    private val secrets = remoteLab.secrets()
    val apiKey = secrets.apiKey
    private val jwtSecret = secrets.jwtSecret
    val jwtCookieName = "jwt-token"

    val apiHeader = "X-API-Key"

    fun buildTestClient(port: Int): WebTestClient = WebTestClient.bindToServer().baseUrl(baseUrl(port)).build()

    private fun baseUrl(port: Int) = "http://localhost:$port"

    // User functions
    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    fun newTestEmail() = "email-${abs(Random.nextLong())}"

    fun randomUserRole() = Role.entries.random().char

    fun newTestOauthId() = "oauth-${abs(Random.nextLong())}"

    fun newTestAccessToken() = "access-token-${abs(Random.nextLong())}"

    fun createTestUser(testClient: WebTestClient): Pair<Int, String> {
        val oAuthId = newTestOauthId()
        val username = newTestUsername()
        val email = newTestEmail()
        val accessToken = newTestAccessToken()

        // when: doing a POST
        // then: the response is an 201 Created
        val res =
            testClient
                .post()
                .uri(Uris.Auth.LOGIN)
                .header(apiHeader, apiKey)
                .bodyValue(
                    mapOf(
                        "oauthId" to oAuthId,
                        "username" to username,
                        "email" to email,
                        "accessToken" to accessToken,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectCookie().exists(jwtCookieName)
                .expectBody<Unit>()
                .returnResult()

        val cookie = res.responseCookies[jwtCookieName]?.first()?.value

        val jwt: DecodedJWT = validateJWTToken(cookie)

        val userId = jwt.getClaim("userId").asString()
        assertNotNull(userId, "User ID should not be null")
        return Pair(userId.toInt(), jwt.token)
    }

    fun validateJWTToken(token: String?): DecodedJWT {
        val verifier: JWTVerifier =
            JWT.require(Algorithm.HMAC256(jwtSecret))
                .withSubject("User Information")
                .withIssuer("Remote Lab")
                .build()

        return assertDoesNotThrow { verifier.verify(token) }
    }

    // Group functions
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    // Lab functions
    val labDomainConfig = remoteLab.laboratoryDomainConfig()

    fun newTestLabName() = "lab-${abs(Random.nextLong())}"

    fun newTestLabDescription() = "description-${abs(Random.nextLong())}"

    fun newTestLabDuration() =
        (labDomainConfig.minLabDuration.toInt(DurationUnit.MINUTES)..labDomainConfig.maxLabDuration.toInt(DurationUnit.MINUTES)).random()

    fun randomLabQueueLimit() = (labDomainConfig.minLabQueueLimit..labDomainConfig.maxLabQueueLimit).random()

    fun randomLabSessionState() = LabSessionState.entries.random()

    // Hardware functions
    fun newTestHardwareName() = HardwareName("hardware-${abs(Random.nextLong())}")

    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"

    fun randomHardwareStatus() = HardwareStatus.entries.random()

    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"

    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}

fun EntityExchangeResult<Problem>.assertProblem(expectedProblem: Problem) {
    assertNotNull(this)
    val problem = this.responseBody
    assertNotNull(problem)
    assertEquals(expectedProblem.type, problem.type)
    assertEquals(expectedProblem.title, problem.title)
    assertEquals(expectedProblem.details, problem.details)
}
