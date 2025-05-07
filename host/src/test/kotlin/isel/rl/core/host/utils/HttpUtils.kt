package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.DurationUnit

class HttpUtils {
    val apiKey: String = "test_api_key"
    private val remoteLabApp = RemoteLabApp()

    val authTokenName = "token"

    val apiHeader = "X-API-Key"

    fun buildTestClient(port: Int): WebTestClient = WebTestClient.bindToServer().baseUrl(baseUrl(port)).build()

    private fun baseUrl(port: Int) = "http://localhost:$port"

    // User functions
    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    fun newTestEmail() = "email-${abs(Random.nextLong())}"

    fun randomUserRole() = Role.entries.random().char

    fun newTestAccessToken() = "access-token-${abs(Random.nextLong())}"

    fun createTestUser(testClient: WebTestClient): String {
        val username = newTestUsername()
        val email = newTestEmail()

        // when: doing a POST
        // then: the response is an 201 Created
        val res =
            testClient
                .post()
                .uri(Uris.Auth.LOGIN)
                .header(apiHeader, apiKey)
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "email" to email,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectCookie().exists(authTokenName)
                .expectBody<SuccessResponse>()
                .returnResult()

        val cookie = res.responseCookies[authTokenName]?.first()?.value
        assertNotNull(cookie)
        return cookie
    }

    // Group functions
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    // Lab functions
    val labDomainConfig: LaboratoriesDomainConfig = remoteLabApp.laboratoriesDomainConfig()

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
    assertEquals(expectedProblem.detail, problem.detail)
}
