package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.Environment
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.host.UsersTests.Companion.USER_OUTPUT_MAP_KEY
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.DurationUnit

typealias UserId = Int
typealias AuthToken = String

class HttpUtils {
    val apiKey: String = "test_api_key"
    private val remoteLab = RemoteLabApp()
    private val userDomainConfig = remoteLab.usersDomainConfig()
    val labDomainConfig = remoteLab.laboratoriesDomainConfig()
    private val groupDomainConfig = remoteLab.groupsDomainConfig()
    private val domainConfigs = remoteLab.domainConfigs
    private val tokenEncoder = remoteLab.tokenEncoder()

    /**
     * Provides a [UsersDomain] instance for validating user-related operations.
     */
    private val usersDomain =
        UsersDomain(
            userDomainConfig,
            tokenEncoder,
        )

    val authHeader = "Authorization"

    val apiHeader = "X-API-Key"

    fun buildTestClient(port: Int): WebTestClient = WebTestClient.bindToServer().baseUrl(baseUrl(port)).build()

    /**
     * Creates a new database connection handle.
     * @return A new [Handle] instance for database operations.
     */
    private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    /**
     * [Jdbi] instance configured with a PostgreSQL data source.
     */
    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements(
            domainConfigs,
        )

    fun createDBUser(
        username: String,
        email: String,
        role: String,
    ): Int {
        var userId: UserId = -1
        runWithHandle { handle ->
            val userRepo = JdbiUsersRepository(handle)

            userId =
                userRepo.createUser(
                    usersDomain.validateCreateUser(
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

    private fun baseUrl(port: Int) = "http://localhost:$port"

    // User functions
    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    fun newTestEmail() = "email-${abs(Random.nextLong())}"

    fun randomUserRole() = Role.entries.random().char

    fun newTestAccessToken() = "access-token-${abs(Random.nextLong())}"

    fun createTestUser(testClient: WebTestClient): Pair<UserId, AuthToken> {
        val username = newTestUsername()
        val email = newTestEmail()

        var userId: UserId = -1
        var token: AuthToken = ""
        // when: doing a POST
        // then: the response is an 201 Created
        testClient
            .post()
            .uri(Uris.Auth.LOGIN)
            .header(apiHeader, apiKey)
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

    // Group functions
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

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
