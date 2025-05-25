package isel.rl.core.host.utils

import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.host.Environment
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object HttpUtils {
    private val remoteLab = RemoteLabApp()
    val domainConfigs = remoteLab.domainConfigs
    private val tokenEncoder = remoteLab.tokenEncoder()
    private val userDomainConfig = remoteLab.usersDomainConfig()
    val labDomainConfig = remoteLab.laboratoriesDomainConfig()
    private val groupDomainConfig = remoteLab.groupsDomainConfig()

    /**
     * Provides a [UsersDomain] instance for validating user-related operations.
     */
    val usersDomain =
        UsersDomain(
            userDomainConfig,
            tokenEncoder,
        )

    /**
     * Provides a [GroupsDomain] instance for validating group-related operations.
     */
    val groupsDomain =
        GroupsDomain(
            groupDomainConfig,
        )

    const val API_KEY_TEST = "test_api_key"
    const val API_HEADER_NAME = "X-API-Key"
    const val AUTH_HEADER_NAME = "Authorization"
    const val AUTH_COOKIE_NAME = "token"

    /**
     * Creates a new database connection handle.
     * @return A new [Handle] instance for database operations.
     */
    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    /**
     * [Jdbi] instance configured with a PostgreSQL data source.
     */
    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements(
            remoteLab.domainConfigs,
        )

    private fun baseUrl(port: Int) = "http://localhost:$port"

    fun buildTestClient(port: Int): WebTestClient = WebTestClient.bindToServer().baseUrl(baseUrl(port)).build()

    @Suppress("UNCHECKED_CAST")
    fun <T> getBodyDataFromResponse(
        response: EntityExchangeResult<SuccessResponse>,
        expectedMessage: String,
        isResponseDataNull: Boolean = false,
    ): T {
        val body = response.responseBody
        assertNotNull(body) { "Response body is null" }
        if (!isResponseDataNull) {
            assert(body.data != null) { "Response data is null" }
        }
        assert(body.message == expectedMessage) {
            "Unexpected message: ${body.message}"
        }

        return body.data as T
    }

    fun assertProblem(
        expectedProblem: Problem,
        actualProblem: EntityExchangeResult<Problem>,
    ) {
        assertNotNull(this)
        val problem = actualProblem.responseBody
        assertNotNull(problem)
        assertEquals(expectedProblem.type, problem.type)
        assertEquals(expectedProblem.title, problem.title)
        assertEquals(expectedProblem.detail, problem.detail)
    }

    /**
     * Returns a random Duration between start (inclusive) and endInclusive (inclusive).
     */
    fun ClosedRange<Duration>.random(random: Random = Random.Default): Duration {
        val minNanos = start.inWholeMinutes
        val maxNanos = endInclusive.inWholeMinutes
        require(maxNanos >= minNanos) { "Invalid Duration range: $start..$endInclusive" }

        val randomNanos = random.nextLong(minNanos, maxNanos + 1)
        return randomNanos.minutes
    }
}
