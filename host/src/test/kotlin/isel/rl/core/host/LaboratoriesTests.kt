package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.assertProblem
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
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
@TestPropertySource(locations = ["classpath:application-test.properties"])
class LaboratoriesTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Nested
    inner class CreateAndRetrieveLaboratoryTests {
        @Test
        fun `create laboratory and retrieve it by id`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val result = testClient.createTestLaboratory()

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                result.authToken,
                result.labId,
                result.initialLab,
            )
        }

        @Test
        fun `get user laboratories (empty)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user
            val (userId, authToken) = httpUtils.createTestUser(testClient)

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(
                        "Laboratories found for user with id $userId",
                        result.responseBody?.message,
                    )
                    assertNotNull(result.responseBody?.data)
                    val laboratories = result.responseBody?.data as List<*>
                    assertTrue(laboratories.isEmpty())
                }
        }

        @Test
        fun `get user laboratories (user is owner)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, authToken) = testClient.createTestLaboratory()

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertNotNull(result.responseBody)
                    assertTrue(
                        result.responseBody!!.message.contains("Laboratories found for user with id"),
                    )
                    assertNotNull(result.responseBody!!.data)
                    val laboratories = result.responseBody!!.data as List<*>
                    assertTrue(laboratories.isNotEmpty())
                    assertTrue(laboratories.size == 1)
                    val laboratory = laboratories[0] as Map<*, *>
                    assertEquals(labId, laboratory["id"])
                    assertEquals(laboratory["labName"], initialLab.labName)
                    assertEquals(laboratory["labDescription"], initialLab.labDescription)
                    assertEquals(laboratory["labDuration"], initialLab.labDuration)
                    assertEquals(laboratory["labQueueLimit"], initialLab.labQueueLimit)
                }
        }

        /*
        TODO: Change this test to use the groups routes
        @Test
        fun `get laboratories by userId`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user and a group
            val (ownerId, jwt) = httpUtils.createTestUser(testClient)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .cookie(httpUtils.jwtCookieName, jwt)
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(
                        "Laboratories found for user with id $ownerId",
                        result.responseBody?.message,
                    )
                    assertNotNull(result.responseBody?.data)
                    val laboratories = result.responseBody?.data as List<*>
                    assertTrue(laboratories.isNotEmpty())
                    assertTrue(laboratories.size == 1)

                    val laboratory = laboratories[0] as Map<*, *>
                    assertEquals(labId, laboratory["laboratoryId"])
                    assertEquals(initialLab.labName, laboratory["labName"])
                    assertEquals(initialLab.labDescription, laboratory["labDescription"])
                    assertEquals(initialLab.labDuration, laboratory["labDuration"])
                    assertEquals(initialLab.labQueueLimit, laboratory["labQueueLimit"])
                    assertEquals(ownerId, laboratory["ownerId"])
                }
        }*/

        @Test
        fun `get laboratory by invalid id`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user
            val (_, authToken) = httpUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, "a")
                .header(httpUtils.authHeader, "Bearer $authToken")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
        }

        @Test
        fun `get non existent laboratory by id`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user
            val (_, authToken) = httpUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, 9999)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .exchange()
                .expectStatus().isNotFound
                .expectBody<Problem>()
        }

        @Test
        fun `create laboratory with invalid labName length (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labName = "",
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabNameProblem)
        }

        @Test
        fun `create laboratory with invalid labName length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labName = "a".repeat(httpUtils.labDomainConfig.maxLabNameLength + 1),
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabNameProblem)
        }

        @Test
        fun `create laboratory with invalid labDescription length (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labDescription = "a",
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `create laboratory with invalid labDescription length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labDescription = "a".repeat(httpUtils.labDomainConfig.maxLabDescriptionLength + 1),
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `create laboratory with invalid labDuration value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labDuration = (httpUtils.labDomainConfig.minLabDuration.inWholeMinutes - 1).toInt(),
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `create laboratory with invalid labDuration value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labDuration = (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt(),
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1,
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabQueueLimitProblem)
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(testClient)

            val invalidLaboratory =
                InitialLaboratory(
                    labQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1,
                )

            // when: creating a laboratory
            testClient.createInvalidLab(authToken, invalidLaboratory, expectedInvalidLabQueueLimitProblem)
        }
    }

    @Nested
    inner class UpdateLaboratoryTestsWithAuthHeader {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()

            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    newLabName,
                    newLabDescription,
                    newLabDuration,
                    newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                jwt,
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labDescription = newLabDescription,
                    labDuration = newLabDuration,
                    labQueueLimit = newLabQueueLimit,
                ),
            )
        }

        @Test
        fun `update laboratory not owned`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, _) = testClient.createTestLaboratory()

            // when: creating a second user
            val (_, authToken) = httpUtils.createTestUser(testClient)

            // when: updating the laboratory
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labName = httpUtils.newTestLabName(),
                )

            // when: updating the laboratory
            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .bodyValue(
                    mapOf(
                        "labName" to updateLab.labName,
                        "labDescription" to updateLab.labDescription,
                        "labDuration" to updateLab.labDuration,
                        "labQueueLimit" to updateLab.labQueueLimit,
                    ),
                )
                .exchange()
                .expectStatus().isNotFound
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(Problem.laboratoryNotFound) }
        }

        @Test
        fun `update laboratory only name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()

            val newLabName = httpUtils.newTestLabName()

            // when: updating the laboratory name
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    newLabName,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(jwt, labId, initialLab.copy(labName = newLabName))
        }

        @Test
        fun `update laboratory only description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDescription = httpUtils.newTestLabDescription()

            // when: updating the laboratory description
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDescription = newLabDescription,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(jwt, labId, initialLab.copy(labDescription = newLabDescription))
        }

        @Test
        fun `update laboratory only duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.newTestLabDuration()

            // when: updating the laboratory duration
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDuration = newLabDuration,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(jwt, labId, initialLab.copy(labDuration = newLabDuration))
        }

        @Test
        fun `update laboratory only queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory queue limit
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labQueueLimit = newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(jwt, labId, initialLab.copy(labQueueLimit = newLabQueueLimit))
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()

            // when: updating the laboratory name and description
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    newLabName,
                    newLabDescription,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                jwt,
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labDescription = newLabDescription,
                ),
            )
        }

        @Test
        fun `update laboratory description and duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()

            // when: updating the laboratory description and duration
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDescription = newLabDescription,
                    labDuration = newLabDuration,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                jwt,
                labId,
                initialLab.copy(
                    labDescription = newLabDescription,
                    labDuration = newLabDuration,
                ),
            )
        }

        @Test
        fun `update laboratory duration and queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory duration and queue limit
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDuration = newLabDuration,
                    labQueueLimit = newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                jwt,
                labId,
                initialLab.copy(
                    labDuration = newLabDuration,
                    labQueueLimit = newLabQueueLimit,
                ),
            )
        }

        @Test
        fun `update laboratory queue limit and name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabName = httpUtils.newTestLabName()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory queue limit and name
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labName = newLabName,
                    labQueueLimit = newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateLab(jwt, labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                jwt,
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labQueueLimit = newLabQueueLimit,
                ),
            )
        }

        @Test
        fun `update laboratory with invalid labName (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabName = "" // Invalid name

            // when: updating the laboratory with invalid name
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labName = newLabName,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabNameProblem)
        }

        @Test
        fun `update laboratory with invalid labName (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabName = "a".repeat(httpUtils.labDomainConfig.maxLabNameLength + 1) // Invalid name

            // when: updating the laboratory with invalid name
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labName = newLabName,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabNameProblem)
        }

        @Test
        fun `update laboratory with invalid labDescription (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDescription = "a" // Invalid description

            // when: updating the laboratory with invalid description
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDescription = newLabDescription,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `update laboratory with invalid labDescription (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDescription =
                "a".repeat(httpUtils.labDomainConfig.maxLabDescriptionLength + 1) // Invalid description

            // when: updating the laboratory with invalid description
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDescription = newLabDescription,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `update laboratory with invalid labDuration (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid duration

            // when: updating the laboratory with invalid duration
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDuration = newLabDuration,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `update laboratory with invalid labDuration (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabDuration =
                (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt() // Invalid duration

            // when: updating the laboratory with invalid duration
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labDuration = newLabDuration,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid queue limit

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labQueueLimit = newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabQueueLimitProblem)
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab, jwt) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1 // Invalid queue limit

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                UpdateLaboratory(
                    initialLab.ownerId,
                    labQueueLimit = newLabQueueLimit,
                )

            // when: updating the laboratory
            testClient.updateInvalidLab(jwt, labId, updateLab, expectedInvalidLabQueueLimitProblem)
        }
    }

    companion object {
        private val httpUtils = HttpUtils()
        private const val LAB_OUTPUT_MAP_KEY = "laboratory"
        private const val UPDATED_SUCCESSFULLY_MSG = "Laboratory updated successfully"
        private const val LAB_NAME_PROP = "labName"
        private const val LAB_DESCRIPTION_PROP = "labDescription"
        private const val LAB_DURATION_PROP = "labDuration"
        private const val LAB_QUEUE_LIMIT_PROP = "labQueueLimit"

        data class InitialLaboratory(
            val ownerId: Int = 0,
            val labName: String? = httpUtils.newTestLabName(),
            val labDescription: String? = httpUtils.newTestLabDescription(),
            val labDuration: Int? = httpUtils.newTestLabDuration(),
            val labQueueLimit: Int? = httpUtils.randomLabQueueLimit(),
        ) {
            fun mapOf() =
                mapOf(
                    LAB_NAME_PROP to labName,
                    LAB_DESCRIPTION_PROP to labDescription,
                    LAB_DURATION_PROP to labDuration,
                    LAB_QUEUE_LIMIT_PROP to labQueueLimit,
                )
        }

        private data class UpdateLaboratory(
            val ownerId: Int,
            val labName: String? = null,
            val labDescription: String? = null,
            val labDuration: Int? = null,
            val labQueueLimit: Int? = null,
        ) {
            fun mapOf() =
                mapOf(
                    LAB_NAME_PROP to labName,
                    LAB_DESCRIPTION_PROP to labDescription,
                    LAB_DURATION_PROP to labDuration,
                    LAB_QUEUE_LIMIT_PROP to labQueueLimit,
                )
        }

        data class CreateTestLabResult(
            val labId: Int,
            val initialLab: InitialLaboratory,
            val authToken: String,
        )

        private fun WebTestClient.createTestLaboratory(): CreateTestLabResult {
            // when: creating a user to be the owner of the laboratory
            val (_, authToken) = httpUtils.createTestUser(this)

            val initialLaboratory = InitialLaboratory()

            // when: creating a laboratory
            var labId = -1
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
                .consumeWith { response ->
                    assertNotNull(response)
                    assertEquals(
                        "Laboratory created successfully",
                        response.responseBody?.message,
                    )
                    assertNotNull(response.responseBody?.data)
                    labId = (response.responseBody?.data as Map<*, *>)["laboratory_id"] as Int
                }
                .returnResult()

            // then: check the labId
            assertTrue(labId >= 0)
            return CreateTestLabResult(
                labId,
                initialLaboratory,
                authToken,
            )
        }

        private fun WebTestClient.createInvalidLab(
            authToken: String,
            initialLaboratory: InitialLaboratory,
            expectedProblem: Problem,
        ) {
            this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .bodyValue(
                    initialLaboratory.mapOf(),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedProblem) }
        }

        private fun WebTestClient.getLabByIdAndVerify(
            authToken: String,
            labId: Int,
            expectedLab: InitialLaboratory,
        ) {
            this.get()
                .uri(Uris.Laboratories.GET, labId)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(
                        "Laboratory found with the id $labId",
                        result.responseBody?.message,
                    )
                    assertNotNull(result.responseBody?.data)
                    val lab =
                        (result.responseBody?.data as Map<*, *>)
                    assertEquals(expectedLab.labName, lab[LAB_NAME_PROP])
                    assertEquals(expectedLab.labDescription, lab[LAB_DESCRIPTION_PROP])
                    assertEquals(expectedLab.labDuration, lab[LAB_DURATION_PROP])
                    assertEquals(expectedLab.labQueueLimit, lab[LAB_QUEUE_LIMIT_PROP])
                }
        }

        private fun WebTestClient.updateLab(
            authToken: String,
            labId: Int,
            updateLab: UpdateLaboratory,
        ) {
            this
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .bodyValue(
                    updateLab.mapOf(),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(
                        UPDATED_SUCCESSFULLY_MSG,
                        result.responseBody?.message,
                    )
                }
        }

        private fun WebTestClient.updateInvalidLab(
            authToken: String,
            labId: Int,
            updateLab: UpdateLaboratory,
            expectedProblem: Problem,
        ) {
            this
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .header(httpUtils.authHeader, "Bearer $authToken")
                .bodyValue(
                    updateLab.mapOf(),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedProblem) }
        }

        private val INVALID_LAB_NAME_MSG =
            "Laboratory name must be between ${httpUtils.labDomainConfig.minLabNameLength} and " +
                "${httpUtils.labDomainConfig.maxLabNameLength} characters"

        val expectedInvalidLabNameProblem =
            Problem.invalidLaboratoryName(
                INVALID_LAB_NAME_MSG,
            )

        private val INVALID_LAB_DESCRIPTION_MSG =
            "Laboratory description must be between ${httpUtils.labDomainConfig.minLabDescriptionLength} " +
                "and ${httpUtils.labDomainConfig.maxLabDescriptionLength} characters"

        val expectedInvalidLabDescriptionProblem =
            Problem.invalidLaboratoryDescription(
                INVALID_LAB_DESCRIPTION_MSG,
            )

        private val INVALID_LAB_DURATION_MSG =
            "Laboratory duration must be between ${httpUtils.labDomainConfig.minLabDuration.inWholeMinutes} and " +
                "${httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes} minutes"

        val expectedInvalidLabDurationProblem =
            Problem.invalidLaboratoryDuration(
                INVALID_LAB_DURATION_MSG,
            )

        private val INVALID_LAB_QUEUE_LIMIT_MSG =
            "Laboratory queue limit must be between ${httpUtils.labDomainConfig.minLabQueueLimit} and " +
                "${httpUtils.labDomainConfig.maxLabQueueLimit}"

        val expectedInvalidLabQueueLimitProblem =
            Problem.invalidLaboratoryQueueLimit(
                INVALID_LAB_QUEUE_LIMIT_MSG,
            )
    }
}
