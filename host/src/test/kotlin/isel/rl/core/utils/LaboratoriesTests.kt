package isel.rl.core.utils

import isel.rl.core.domain.Uris
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.laboratory.LaboratoryOutputModel
import org.junit.jupiter.api.Nested
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
            val (labId, initialLab) = testClient.createTestLaboratory()

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(labId, initialLab)
        }

        @Test
        fun `get laboratory by invalid id`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, "a")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
        }

        @Test
        fun `get non existent laboratory by id`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, 9999)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<Problem>()
        }

        @Test
        fun `create laboratory with invalid labName length (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labName = ""
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabNameProblem)
        }

        @Test
        fun `create laboratory with invalid labName length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labName = "a".repeat(httpUtils.labDomainConfig.maxLengthLabName + 1)
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabNameProblem)
        }

        @Test
        fun `create laboratory with invalid labDescription length (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labDescription = ""
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `create laboratory with invalid labDescription length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labDescription = "a".repeat(httpUtils.labDomainConfig.maxLengthLabDescription + 1)
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `create laboratory with invalid labDuration value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labDuration = (httpUtils.labDomainConfig.minLabDuration.inWholeMinutes - 1).toInt()
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `create laboratory with invalid labDuration value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labDuration = (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt()
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabQueueLimitProblem)
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val invalidLaboratory = InitialLaboratory(
                ownerId,
                labQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1
            )

            // when: creating a laboratory
            testClient.createInvalidLab(invalidLaboratory, expectedInvalidLabQueueLimitProblem)
        }
    }

    @Nested
    inner class UpdateLaboratoryTests {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()

            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                newLabName,
                newLabDescription,
                newLabDuration,
                newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labDescription = newLabDescription,
                    labDuration = newLabDuration,
                    labQueueLimit = newLabQueueLimit
                )
            )
        }

        @Test
        fun `update laboratory only name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()

            val newLabName = httpUtils.newTestLabName()

            // when: updating the laboratory name
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                newLabName,
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(labId, initialLab.copy(labName = newLabName))
        }

        @Test
        fun `update laboratory only description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDescription = httpUtils.newTestLabDescription()

            // when: updating the laboratory description
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDescription = newLabDescription
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(labId, initialLab.copy(labDescription = newLabDescription))
        }

        @Test
        fun `update laboratory only duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.newTestLabDuration()

            // when: updating the laboratory duration
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDuration = newLabDuration
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(labId, initialLab.copy(labDuration = newLabDuration))
        }

        @Test
        fun `update laboratory only queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory queue limit
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labQueueLimit = newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(labId, initialLab.copy(labQueueLimit = newLabQueueLimit))
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()

            // when: updating the laboratory name and description
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                newLabName,
                newLabDescription
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labDescription = newLabDescription
                )
            )
        }

        @Test
        fun `update laboratory description and duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()

            // when: updating the laboratory description and duration
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDescription = newLabDescription,
                labDuration = newLabDuration
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                labId,
                initialLab.copy(
                    labDescription = newLabDescription,
                    labDuration = newLabDuration
                )
            )
        }

        @Test
        fun `update laboratory duration and queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory duration and queue limit
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDuration = newLabDuration,
                labQueueLimit = newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                labId,
                initialLab.copy(
                    labDuration = newLabDuration,
                    labQueueLimit = newLabQueueLimit
                )
            )
        }

        @Test
        fun `update laboratory queue limit and name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabName = httpUtils.newTestLabName()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            // when: updating the laboratory queue limit and name
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labName = newLabName,
                labQueueLimit = newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateLab(labId, updateLab)

            // when: retrieving the laboratory by id
            testClient.getLabByIdAndVerify(
                labId,
                initialLab.copy(
                    labName = newLabName,
                    labQueueLimit = newLabQueueLimit
                )
            )
        }

        @Test
        fun `update laboratory with invalid labName (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabName = "" // Invalid name

            // when: updating the laboratory with invalid name
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labName = newLabName
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabNameProblem)
        }

        @Test
        fun `update laboratory with invalid labName (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabName = "a".repeat(httpUtils.labDomainConfig.maxLengthLabName + 1) // Invalid name

            // when: updating the laboratory with invalid name
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labName = newLabName
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabNameProblem)
        }

        @Test
        fun `update laboratory with invalid labDescription (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDescription = "" // Invalid description

            // when: updating the laboratory with invalid description
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDescription = newLabDescription
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `update laboratory with invalid labDescription (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDescription =
                "a".repeat(httpUtils.labDomainConfig.maxLengthLabDescription + 1) // Invalid description

            // when: updating the laboratory with invalid description
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDescription = newLabDescription
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabDescriptionProblem)
        }

        @Test
        fun `update laboratory with invalid labDuration (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDuration = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid duration

            // when: updating the laboratory with invalid duration
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDuration = newLabDuration
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `update laboratory with invalid labDuration (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabDuration =
                (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt() // Invalid duration

            // when: updating the laboratory with invalid duration
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labDuration = newLabDuration
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabDurationProblem)
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid queue limit

            // when: updating the laboratory with invalid queue limit
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labQueueLimit = newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabQueueLimitProblem)
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (labId, initialLab) = testClient.createTestLaboratory()
            val newLabQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1 // Invalid queue limit

            // when: updating the laboratory with invalid queue limit
            val updateLab = UpdateLaboratory(
                initialLab.ownerId,
                labQueueLimit = newLabQueueLimit
            )

            // when: updating the laboratory
            testClient.updateInvalidLab(labId, updateLab, expectedInvalidLabQueueLimitProblem)
        }
    }

    companion object {
        private val httpUtils = HttpUtils()
        private const val LAB_OUTPUT_MAP_KEY = "laboratory"
        private const val UPDATED_SUCCESSFULLY_MSG = "Laboratory updated successfully"

        private data class InitialLaboratory(
            val ownerId: Int,
            val labName: String? = httpUtils.newTestLabName(),
            val labDescription: String? = httpUtils.newTestLabDescription(),
            val labDuration: Int? = httpUtils.newTestLabDuration(),
            val labQueueLimit: Int? = httpUtils.randomLabQueueLimit(),
        )

        private data class UpdateLaboratory(
            val ownerId: Int,
            val labName: String? = null,
            val labDescription: String? = null,
            val labDuration: Int? = null,
            val labQueueLimit: Int? = null,
        )

        private fun WebTestClient.createTestLaboratory(): Pair<Int, InitialLaboratory> {
            // create a owner user
            val ownerId = httpUtils.createTestUser(this)

            val initialLaboratory = InitialLaboratory(ownerId)

            // when: creating a laboratory
            val responseLabId = this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to initialLaboratory.labName,
                        "labDescription" to initialLaboratory.labDescription,
                        "labDuration" to initialLaboratory.labDuration,
                        "labQueueLimit" to initialLaboratory.labQueueLimit,
                        "ownerId" to ownerId
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)
            return Pair(labId, initialLaboratory)
        }

        private fun WebTestClient.createInvalidLab(initialLaboratory: InitialLaboratory, expectedProblem: Problem) {
            this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    initialLaboratory
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedProblem) }
        }

        private fun WebTestClient.getLabByIdAndVerify(
            labId: Int,
            expectedLab: InitialLaboratory
        ) {
            this.get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(expectedLab.labName, lab.labName)
                    assertEquals(expectedLab.labDescription, lab.labDescription)
                    assertEquals(expectedLab.labDuration, lab.labDuration)
                    assertEquals(expectedLab.labQueueLimit, lab.labQueueLimit)
                    assertEquals(expectedLab.ownerId, lab.ownerId)
                }
        }

        private fun WebTestClient.updateLab(
            labId: Int,
            updateLab: UpdateLaboratory
        ) {
            this
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to updateLab.labName,
                        "labDescription" to updateLab.labDescription,
                        "labDuration" to updateLab.labDuration,
                        "labQueueLimit" to updateLab.labQueueLimit,
                        "ownerId" to updateLab.ownerId
                    )
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }
        }

        private fun WebTestClient.updateInvalidLab(
            labId: Int,
            updateLab: UpdateLaboratory,
            expectedProblem: Problem
        ) {
            this
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to updateLab.labName,
                        "labDescription" to updateLab.labDescription,
                        "labDuration" to updateLab.labDuration,
                        "labQueueLimit" to updateLab.labQueueLimit,
                        "ownerId" to updateLab.ownerId
                    )
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedProblem) }
        }


        private val INVALID_LAB_NAME_MSG =
            "Laboratory name must be between ${httpUtils.labDomainConfig.minLengthLabName} and ${httpUtils.labDomainConfig.maxLengthLabName} characters"

        val expectedInvalidLabNameProblem = Problem.invalidLaboratoryName(
            INVALID_LAB_NAME_MSG
        )

        private val INVALID_LAB_DESCRIPTION_MSG =
            "Laboratory description must be between ${httpUtils.labDomainConfig.minLengthLabDescription} and ${httpUtils.labDomainConfig.maxLengthLabDescription} characters"

        val expectedInvalidLabDescriptionProblem = Problem.invalidLaboratoryDescription(
            INVALID_LAB_DESCRIPTION_MSG
        )

        private val INVALID_LAB_DURATION_MSG =
            "Laboratory duration must be between ${httpUtils.labDomainConfig.minLabDuration.inWholeMinutes} and ${httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes} minutes"

        val expectedInvalidLabDurationProblem = Problem.invalidLaboratoryDuration(
            INVALID_LAB_DURATION_MSG
        )

        private val INVALID_LAB_QUEUE_LIMIT_MSG =
            "Laboratory queue limit must be between ${httpUtils.labDomainConfig.minLabQueueLimit} and ${httpUtils.labDomainConfig.maxLabQueueLimit}"

        val expectedInvalidLabQueueLimitProblem = Problem.invalidLaboratoryQueueLimit(
            INVALID_LAB_QUEUE_LIMIT_MSG
        )
    }
}