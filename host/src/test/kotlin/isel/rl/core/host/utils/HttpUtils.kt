package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.Environment
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.host.UsersTests.Companion.USER_OUTPUT_MAP_KEY
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Instant
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

typealias UserId = Int
typealias AuthToken = String

object HttpUtilsTest {
    private fun baseUrl(port: Int) = "http://localhost:$port"

    fun buildTestClient(port: Int): WebTestClient = WebTestClient.bindToServer().baseUrl(baseUrl(port)).build()

    private val remoteLab = RemoteLabApp()
    private val tokenEncoder = remoteLab.tokenEncoder()
    private val userDomainConfig = remoteLab.usersDomainConfig()
    val labDomainConfig = remoteLab.laboratoriesDomainConfig()
    private val groupDomainConfig = remoteLab.groupsDomainConfig()

    /**
     * Provides a [UsersDomain] instance for validating user-related operations.
     */
    private val usersDomain =
        UsersDomain(
            userDomainConfig,
            tokenEncoder,
        )

    const val API_KEY_TEST = "test_api_key"
    const val API_HEADER_NAME = "X-API-Key"
    const val AUTH_HEADER_NAME = "Authorization"
    const val AUTH_COOKIE_NAME = "token"

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
            remoteLab.domainConfigs,
        )

    fun getBodyDataFromResponse(
        response: EntityExchangeResult<SuccessResponse>,
        expectedMessage: String,
    ): Map<*, *> {
        val body = response.responseBody
        assertNotNull(body) { "Response body is null" }
        assert(body.data != null) { "Response data is null" }
        assert(body.message == expectedMessage) {
            "Unexpected message: ${body.message}"
        }

        return body.data as Map<*, *>
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

    object Users {
        private const val USER_OUTPUT_MAP_KEY = "user"
        private const val TOKEN_OUTPUT_MAP_KEY = "token"
        private const val ID_PROP = "id"
        private const val ROLE_PROP = "role"
        private const val NAME_PROP = "name"
        private const val EMAIL_PROP = "email"
        private const val CREATED_AT_PROP = "createdAt"

        data class InitialUser(
            val id: Int = 0,
            val role: Role = randomUserRole(),
            val name: Name = newTestUsername(),
            val email: Email = newTestEmail(),
            val createdAt: Instant = Instant.DISTANT_PAST,
            val authToken: String = "",
        ) {
            companion object {
                fun createBodyValue(initialUser: InitialUser) =
                    mapOf(
                        "name" to initialUser.name.nameInfo,
                        "email" to initialUser.email.emailInfo,
                    )
            }
        }

        fun newTestUsername() = Name("user-${abs(Random.nextLong())}")

        fun newTestEmail() = Email("email-${abs(Random.nextLong())}")

        fun randomUserRole() = Role.entries.random()

        fun createTestUser(
            testClient: WebTestClient,
            initialUser: InitialUser = InitialUser(),
        ): InitialUser {
            val response =
                testClient.post()
                    .uri(Uris.Auth.LOGIN)
                    .header(API_HEADER_NAME, API_KEY_TEST)
                    .bodyValue(InitialUser.createBodyValue(initialUser))
                    .exchange()
                    .expectStatus().isOk
                    .expectCookie().exists(AUTH_COOKIE_NAME)
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val data = getBodyDataFromResponse(response, "User logged in successfully")
            val user = data[USER_OUTPUT_MAP_KEY] as Map<*, *>
            val token = data[TOKEN_OUTPUT_MAP_KEY] as String

            assertEquals(initialUser.name.nameInfo, user[NAME_PROP], "User name mismatch")
            assertEquals(initialUser.email.emailInfo, user[EMAIL_PROP], "User email mismatch")

            return initialUser.copy(
                id = user[ID_PROP] as Int,
                role = Role.entries.firstOrNull { it.char == user[ROLE_PROP] as String }!!,
                createdAt = Instant.parse(user[CREATED_AT_PROP] as String),
                authToken = token,
            )
        }

        fun createInvalidUser(
            testClient: WebTestClient,
            initialUser: InitialUser,
            expectedProblem: Problem,
        ) {
            testClient
                .post()
                .uri(Uris.Auth.LOGIN)
                .header(API_HEADER_NAME, API_KEY_TEST)
                .bodyValue(
                    InitialUser.createBodyValue(initialUser),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { assertProblem(expectedProblem, it) }
        }

        fun getUserById(
            testClient: WebTestClient,
            expectedUser: InitialUser,
        ) {
            val response =
                testClient.get()
                    .uri(Uris.Users.GET, expectedUser.id)
                    .header(AUTH_HEADER_NAME, "Bearer ${expectedUser.authToken}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val user = getBodyDataFromResponse(response, "User found with the id ${expectedUser.id}")

            assertEquals(expectedUser.id, user[ID_PROP], "User id mismatch")
            assertEquals(expectedUser.name.nameInfo, user[NAME_PROP], "User name mismatch")
            assertEquals(expectedUser.email.emailInfo, user[EMAIL_PROP], "User email mismatch")
            assertEquals(expectedUser.role.char, user[ROLE_PROP], "User role mismatch")
            assertEquals(
                expectedUser.createdAt.toEpochMilliseconds(),
                Instant.parse(user[CREATED_AT_PROP] as String).toEpochMilliseconds(),
                "User createdAt mismatch",
            )
        }

        fun getUserByEmail(
            testClient: WebTestClient,
            expectedUser: InitialUser,
        ) {
            val response =
                testClient.get()
                    .uri { builder ->
                        builder
                            .path(Uris.Users.GET_BY_EMAIL)
                            .queryParam("email", expectedUser.email.emailInfo)
                            .build()
                    }
                    .header(AUTH_HEADER_NAME, "Bearer ${expectedUser.authToken}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val user = getBodyDataFromResponse(response, "User found with the email ${expectedUser.email.emailInfo}")

            assertEquals(expectedUser.id, user[ID_PROP], "User id mismatch")
            assertEquals(expectedUser.name.nameInfo, user[NAME_PROP], "User name mismatch")
            assertEquals(expectedUser.email.emailInfo, user[EMAIL_PROP], "User email mismatch")
            assertEquals(expectedUser.role.char, user[ROLE_PROP], "User role mismatch")
            assertEquals(
                expectedUser.createdAt.toEpochMilliseconds(),
                Instant.parse(user[CREATED_AT_PROP] as String).toEpochMilliseconds(),
                "User createdAt mismatch",
            )
        }

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
    }

    object Laboratories {
        const val ID_PROP = "id"
        const val LAB_NAME_PROP = "labName"
        const val LAB_DESCRIPTION_PROP = "labDescription"
        const val LAB_DURATION_PROP = "labDuration"
        const val LAB_QUEUE_LIMIT_PROP = "labQueueLimit"
        const val CREATED_AT_PROP = "createdAt"
        const val OWNER_ID_PROP = "ownerId"

        data class InitialLab(
            val id: Int = 0,
            val name: LabName = newTestLabName(),
            val description: LabDescription? = newTestLabDescription(),
            val duration: LabDuration? = newTestLabDuration(),
            val queueLimit: LabQueueLimit? = randomLabQueueLimit(),
            val createdAt: Instant = Instant.DISTANT_PAST,
            val ownerId: Int = 0,
        ) {
            companion object {
                fun createBodyValue(initialLab: InitialLab) =
                    mapOf(
                        LAB_NAME_PROP to initialLab.name.labNameInfo,
                        LAB_DESCRIPTION_PROP to initialLab.description?.labDescriptionInfo,
                        LAB_DURATION_PROP to initialLab.duration?.labDurationInfo?.inWholeMinutes,
                        LAB_QUEUE_LIMIT_PROP to initialLab.queueLimit?.labQueueLimitInfo,
                    )
            }
        }

        fun newTestLabName() = LabName("lab-${abs(Random.nextLong())}")

        fun newTestLabDescription() = LabDescription("description-${abs(Random.nextLong())}")

        fun newTestLabDuration() =
            LabDuration(
                (labDomainConfig.minLabDuration..labDomainConfig.maxLabDuration).random(),
            )

        fun randomLabQueueLimit() = LabQueueLimit((labDomainConfig.minLabQueueLimit..labDomainConfig.maxLabQueueLimit).random())

        fun createLab(
            testClient: WebTestClient,
            authToken: String,
            initialLab: InitialLab = InitialLab(),
        ): InitialLab {
            val response =
                testClient.post()
                    .uri(Uris.Laboratories.CREATE)
                    .header(AUTH_HEADER_NAME, "Bearer $authToken")
                    .bodyValue(InitialLab.createBodyValue(initialLab))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val lab = getBodyDataFromResponse(response, "Laboratory created successfully")

            assertEquals(initialLab.name.labNameInfo, lab[LAB_NAME_PROP], "Lab name mismatch")
            assertEquals(
                initialLab.description?.labDescriptionInfo,
                lab[LAB_DESCRIPTION_PROP],
                "Lab description mismatch",
            )
            assertEquals(
                initialLab.duration?.labDurationInfo?.inWholeMinutes?.toInt(),
                lab[LAB_DURATION_PROP],
                "Lab duration mismatch",
            )
            assertEquals(
                initialLab.queueLimit?.labQueueLimitInfo,
                lab[LAB_QUEUE_LIMIT_PROP],
                "Lab queue limit mismatch",
            )

            return initialLab.copy(
                id = lab[ID_PROP] as Int,
                createdAt = Instant.parse(lab[CREATED_AT_PROP] as String),
                ownerId = lab[OWNER_ID_PROP] as Int,
            )
        }

        fun createInvalidLab(
            testClient: WebTestClient,
            authToken: String,
            initialLaboratory: InitialLab,
            expectedProblem: Problem,
        ) {
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .bodyValue(
                    InitialLab.createBodyValue(initialLaboratory),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { assertProblem(expectedProblem, it) }
        }

        fun getLabById(
            testClient: WebTestClient,
            authToken: String,
            expectedLab: InitialLab,
        ) {
            val response =
                testClient.get()
                    .uri(Uris.Laboratories.GET, expectedLab.id)
                    .header(AUTH_HEADER_NAME, "Bearer $authToken")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val lab = getBodyDataFromResponse(response, "Laboratory found with the id ${expectedLab.id}")

            assertEquals(expectedLab.id, lab[ID_PROP], "Lab id mismatch")
            assertEquals(expectedLab.name.labNameInfo, lab[LAB_NAME_PROP], "Lab name mismatch")
            assertEquals(
                expectedLab.description?.labDescriptionInfo,
                lab[LAB_DESCRIPTION_PROP],
                "Lab description mismatch",
            )
            assertEquals(
                expectedLab.duration?.labDurationInfo?.inWholeMinutes?.toInt(),
                lab[LAB_DURATION_PROP],
                "Lab duration mismatch",
            )
            assertEquals(
                expectedLab.queueLimit?.labQueueLimitInfo,
                lab[LAB_QUEUE_LIMIT_PROP],
                "Lab queue limit mismatch",
            )
            assertEquals(
                expectedLab.createdAt.toEpochMilliseconds(),
                Instant.parse(lab[CREATED_AT_PROP] as String).toEpochMilliseconds(),
                "Lab createdAt mismatch",
            )
            assertEquals(expectedLab.ownerId, lab[OWNER_ID_PROP], "Lab ownerId mismatch")
        }

        fun updateLab(
            testClient: WebTestClient,
            authToken: String,
            updateLab: InitialLab,
        ) {
            val response =
                testClient.patch()
                    .uri(Uris.Laboratories.UPDATE, updateLab.id)
                    .header(AUTH_HEADER_NAME, "Bearer $authToken")
                    .bodyValue(InitialLab.createBodyValue(updateLab))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<SuccessResponse>()
                    .returnResult()

            assertNotNull(response.responseBody)
            assertEquals(
                "Laboratory updated successfully",
                response.responseBody!!.message,
                "Expected update message but got ${response.responseBody!!.message}",
            )
        }

        fun updateInvalidLab(
            testClient: WebTestClient,
            authToken: String,
            updateLab: InitialLab,
            expectedProblem: Problem,
        ) {
            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, updateLab.id)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .bodyValue(
                    InitialLab.createBodyValue(updateLab),
                )
                .exchange()
                .expectStatus().apply {
                    if (expectedProblem == Problem.laboratoryNotFound) {
                        isNotFound
                    } else {
                        isBadRequest
                    }
                        .expectBody<Problem>()
                        .consumeWith { assertProblem(expectedProblem, it) }
                }
        }

        private val INVALID_LAB_NAME_MSG =
            "Laboratory name must be between ${labDomainConfig.minLabNameLength} and " +
                "${labDomainConfig.maxLabNameLength} characters"

        val expectedInvalidLabNameProblem =
            Problem.invalidLaboratoryName(
                INVALID_LAB_NAME_MSG,
            )

        val expectedRequiredLabNameProblem =
            Problem.invalidLaboratoryName(
                "Laboratory name is required",
            )

        private val INVALID_LAB_DESCRIPTION_MSG =
            "Laboratory description must be between ${labDomainConfig.minLabDescriptionLength} " +
                "and ${labDomainConfig.maxLabDescriptionLength} characters"

        val expectedInvalidLabDescriptionProblem =
            Problem.invalidLaboratoryDescription(
                INVALID_LAB_DESCRIPTION_MSG,
            )

        private val INVALID_LAB_DURATION_MSG =
            "Laboratory duration must be between ${labDomainConfig.minLabDuration.inWholeMinutes} and " +
                "${labDomainConfig.maxLabDuration.inWholeMinutes} minutes"

        val expectedInvalidLabDurationProblem =
            Problem.invalidLaboratoryDuration(
                INVALID_LAB_DURATION_MSG,
            )

        private val INVALID_LAB_QUEUE_LIMIT_MSG =
            "Laboratory queue limit must be between ${labDomainConfig.minLabQueueLimit} and " +
                "${labDomainConfig.maxLabQueueLimit}"

        val expectedInvalidLabQueueLimitProblem =
            Problem.invalidLaboratoryQueueLimit(
                INVALID_LAB_QUEUE_LIMIT_MSG,
            )
    }

    object Groups {
        const val ID_PROP = "id"
        const val GROUP_NAME_PROP = "groupName"
        const val GROUP_DESCRIPTION_PROP = "groupDescription"
        const val CREATED_AT_PROP = "createdAt"
        const val OWNER_ID_PROP = "ownerId"

        data class InitialGroup(
            val id: Int = 0,
            val name: GroupName = newTestGroupName(),
            val description: GroupDescription = newTestGroupDescription(),
            val createdAt: Instant = Instant.DISTANT_PAST,
            val ownerId: Int = 0,
        ) {
            companion object {
                fun createBodyValue(initialGroup: InitialGroup) =
                    mapOf(
                        GROUP_NAME_PROP to initialGroup.name.groupNameInfo,
                        GROUP_DESCRIPTION_PROP to initialGroup.description.groupDescriptionInfo,
                    )
            }
        }

        fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

        fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

        fun createGroup(
            testClient: WebTestClient,
            initialGroup: InitialGroup,
            authToken: String = "",
        ): InitialGroup {
            val response =
                testClient.post()
                    .uri(Uris.Groups.CREATE)
                    .header(AUTH_HEADER_NAME, "Bearer $authToken")
                    .bodyValue(InitialGroup.createBodyValue(initialGroup))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<SuccessResponse>()
                    .returnResult()

            val group = getBodyDataFromResponse(response, "Group created successfully")
            assertEquals(initialGroup.name.groupNameInfo, group[GROUP_NAME_PROP], "Group name mismatch")
            assertEquals(
                initialGroup.description.groupDescriptionInfo,
                group[GROUP_DESCRIPTION_PROP],
                "Group description mismatch",
            )

            return initialGroup.copy(
                id = group[ID_PROP] as Int,
                createdAt = Instant.parse(group[CREATED_AT_PROP] as String),
                ownerId = group[OWNER_ID_PROP] as Int,
            )
        }
    }
}

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

    // Group related
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
