package isel.rl.core.utils

import isel.rl.core.domain.Uris
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.laboratory.LaboratoryOutputModel
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.EntityExchangeResult
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

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
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

            val labName = "" // Invalid labName
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabNameProblem) }
        }

        @Test
        fun `create laboratory with invalid labName length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = "a".repeat(httpUtils.labDomainConfig.maxLengthLabName + 1) // Invalid labName
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabNameProblem) }
        }

        @Test
        fun `create laboratory with invalid labDescription length (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = "" // Invalid labDescription
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDescriptionProblem) }
        }

        @Test
        fun `create laboratory with invalid labDescription length (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = "a".repeat(httpUtils.labDomainConfig.maxLengthLabDescription + 1) // Invalid labDescription
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDescriptionProblem) }
        }

        @Test
        fun `create laboratory with invalid labDuration value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid labDuration
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDurationProblem) }
        }

        @Test
        fun `create laboratory with invalid labDuration value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt() // Invalid labDuration
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDurationProblem) }
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid labQueueLimit

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabQueueLimitProblem) }
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1 // Invalid labQueueLimit

            // when: creating a laboratory
            testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabQueueLimitProblem) }
        }
    }

    @Nested
    inner class UpdateLaboratoryTests {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory
            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to newLabName,
                        "labDescription" to newLabDescription,
                        "labDuration" to newLabDuration,
                        "labQueueLimit" to newLabQueueLimit,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(newLabName, lab.labName)
                    assertEquals(newLabDescription, lab.labDescription)
                    assertEquals(newLabDuration, lab.labDuration)
                    assertEquals(newLabQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory only name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory name
            val newLabName = httpUtils.newTestLabName()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to newLabName,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(newLabName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory only description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory description
            val newLabDescription = httpUtils.newTestLabDescription()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDescription" to newLabDescription,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(newLabDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory only duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory duration
            val newLabDuration = httpUtils.newTestLabDuration()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDuration" to newLabDuration,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(newLabDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory only queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory queue limit
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labQueueLimit" to newLabQueueLimit,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(newLabQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory name and description
            val newLabName = httpUtils.newTestLabName()
            val newLabDescription = httpUtils.newTestLabDescription()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to newLabName,
                        "labDescription" to newLabDescription,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(newLabName, lab.labName)
                    assertEquals(newLabDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory description and duration`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory description and duration
            val newLabDescription = httpUtils.newTestLabDescription()
            val newLabDuration = httpUtils.newTestLabDuration()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDescription" to newLabDescription,
                        "labDuration" to newLabDuration,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(newLabDescription, lab.labDescription)
                    assertEquals(newLabDuration, lab.labDuration)
                    assertEquals(labQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory duration and queue limit`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory duration and queue limit
            val newLabDuration = httpUtils.newTestLabDuration()
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDuration" to newLabDuration,
                        "labQueueLimit" to newLabQueueLimit,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(labName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(newLabDuration, lab.labDuration)
                    assertEquals(newLabQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory queue limit and name`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val ownerId = httpUtils.createTestUser(testClient)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = testClient
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)

            // when: updating the laboratory queue limit and name
            val newLabQueueLimit = httpUtils.randomLabQueueLimit()
            val newLabName = httpUtils.newTestLabName()

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labQueueLimit" to newLabQueueLimit,
                        "labName" to newLabName,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody<String>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(UPDATED_SUCCESSFULLY_MSG, result.responseBody)
                }

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, labId)
                .exchange()
                .expectStatus().isOk
                .expectBody<Map<String, LaboratoryOutputModel>>()
                .consumeWith { result ->
                    assertNotNull(result)
                    val lab = result.responseBody?.get(LAB_OUTPUT_MAP_KEY)
                    assertNotNull(lab)
                    assertEquals(newLabName, lab.labName)
                    assertEquals(labDescription, lab.labDescription)
                    assertEquals(labDuration, lab.labDuration)
                    assertEquals(newLabQueueLimit, lab.labQueueLimit)
                    assertEquals(ownerId, lab.ownerId)
                }
        }

        @Test
        fun `update laboratory with invalid labName (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid name
            val newLabName = "" // Invalid name

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to newLabName,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabNameProblem) }
        }

        @Test
        fun `update laboratory with invalid labName (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid name
            val newLabName = "a".repeat(httpUtils.labDomainConfig.maxLengthLabName + 1) // Invalid name

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labName" to newLabName,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabNameProblem) }
        }

        @Test
        fun `update laboratory with invalid labDescription (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid description
            val newLabDescription = "" // Invalid description

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDescription" to newLabDescription,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDescriptionProblem) }
        }

        @Test
        fun `update laboratory with invalid labDescription (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid description
            val newLabDescription = "a".repeat(httpUtils.labDomainConfig.maxLengthLabDescription + 1) // Invalid description

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDescription" to newLabDescription,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDescriptionProblem) }
        }

        @Test
        fun `update laboratory with invalid labDuration (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid duration
            val newLabDuration = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid duration

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDuration" to newLabDuration,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDurationProblem) }
        }

        @Test
        fun `update laboratory with invalid labDuration (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid duration
            val newLabDuration = (httpUtils.labDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt() // Invalid duration

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labDuration" to newLabDuration,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabDurationProblem) }
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (min)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid queue limit
            val newLabQueueLimit = httpUtils.labDomainConfig.minLabQueueLimit - 1 // Invalid queue limit

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labQueueLimit" to newLabQueueLimit,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabQueueLimitProblem) }
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (max)`() {
            // given: a test client
            val testClient = httpUtils.buildTestClient(port)

            // when: creating a laboratory
            val (ownerId, labId) = testClient.createTestLaboratory()

            // when: updating the laboratory with invalid queue limit
            val newLabQueueLimit = httpUtils.labDomainConfig.maxLabQueueLimit + 1 // Invalid queue limit

            testClient
                .patch()
                .uri(Uris.Laboratories.UPDATE, labId)
                .bodyValue(
                    mapOf(
                        "labQueueLimit" to newLabQueueLimit,
                        "userId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { it.assertProblem(expectedInvalidLabQueueLimitProblem) }
        }
    }

    companion object {
        private val httpUtils = HttpUtils()
        const val LAB_OUTPUT_MAP_KEY = "laboratory"
        const val UPDATED_SUCCESSFULLY_MSG = "Laboratory updated successfully"

        fun EntityExchangeResult<Problem>.assertProblem(expectedProblem: Problem) {
            assertNotNull(this)
            val problem = this.responseBody
            assertNotNull(problem)
            assertEquals(expectedProblem.type, problem.type)
            assertEquals(expectedProblem.title, problem.title)
            assertEquals(expectedProblem.details, problem.details)
        }

        fun WebTestClient.createTestLaboratory(): Pair<Int, Int> {
            // create a owner user
            val ownerId = httpUtils.createTestUser(this)

            val labName = httpUtils.newTestLabName()
            val labDescription = httpUtils.newTestLabDescription()
            val labDuration = httpUtils.newTestLabDuration()
            val labQueueLimit = httpUtils.randomLabQueueLimit()

            // when: creating a laboratory
            val responseLabId = this
                .post()
                .uri(Uris.Laboratories.CREATE)
                .bodyValue(
                    mapOf(
                        "labName" to labName,
                        "labDescription" to labDescription,
                        "labDuration" to labDuration,
                        "labQueueLimit" to labQueueLimit,
                        "ownerId" to ownerId
                    ),
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<Int>()
                .returnResult()

            // then: check the labId
            val labId = responseLabId.responseBody!!
            assertTrue(labId >= 0)
            return Pair(ownerId, labId)
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