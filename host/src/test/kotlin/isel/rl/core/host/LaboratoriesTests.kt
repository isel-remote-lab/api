package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.LabsTestsUtils
import isel.rl.core.host.utils.UsersTestsUtils
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

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
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient = testClient, authToken = user.authToken)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, lab)
        }

        @Test
        fun `get user laboratories (empty)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .consumeWith { result ->
                    assertNotNull(result)
                    assertEquals(
                        "Laboratories found for user with id ${user.id}",
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
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
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
                    assertEquals(lab.id, laboratory["id"])
                    assertEquals(lab.name.labNameInfo, laboratory["labName"])
                    assertEquals(lab.description?.labDescriptionInfo, laboratory["labDescription"])
                    assertEquals(lab.duration?.labDurationInfo?.inWholeMinutes?.toInt(), laboratory["labDuration"])
                    assertEquals(lab.queueLimit?.labQueueLimitInfo, laboratory["labQueueLimit"])
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
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, "a")
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { HttpUtils.assertProblem(Problem.invalidLaboratoryId, it) }
        }

        @Test
        fun `get non existent laboratory by id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, 9999)
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
                .exchange()
                .expectStatus().isNotFound
                .expectBody<Problem>()
                .consumeWith { HttpUtils.assertProblem(Problem.laboratoryNotFound, it) }
        }

        @Test
        fun `create laboratory with invalid labName length (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    name = LabName(""),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedRequiredLabNameProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labName length (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    name = LabName("a".repeat(HttpUtils.labDomainConfig.maxLabNameLength + 1)),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDescription length (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    description = LabDescription("a"),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDescription length (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    description = LabDescription("a".repeat(HttpUtils.labDomainConfig.maxLabDescriptionLength + 1)),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDuration value (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.minLabDuration.minus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDuration value (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.maxLabDuration.plus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.minLabQueueLimit - 1),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.maxLabQueueLimit + 1),
                )

            // when: creating a laboratory
            LabsTestsUtils.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }
    }

    @Nested
    inner class UpdateLaboratoryTestsWithAuthHeader {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    createdAt = lab.createdAt,
                    ownerId = user.id,
                )
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id adn verify
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = UsersTestsUtils.createTestUser(testClient)
            val user2 = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = LabsTestsUtils.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(testClient, user2.authToken, updateLab, Problem.laboratoryNotFound)
        }

        @Test
        fun `update laboratory only name`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory name
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = LabsTestsUtils.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only description`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory description
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    description = LabsTestsUtils.newTestLabDescription(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only duration`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory duration
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    duration = LabsTestsUtils.newTestLabDuration(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only queue limit`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory queue limit
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    queueLimit = LabsTestsUtils.randomLabQueueLimit(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory name and description
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = LabsTestsUtils.newTestLabName(),
                    description = LabsTestsUtils.newTestLabDescription(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory description and duration`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory description and duration
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    description = LabsTestsUtils.newTestLabDescription(),
                    duration = LabsTestsUtils.newTestLabDuration(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory duration and queue limit`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory duration and queue limit
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    duration = LabsTestsUtils.newTestLabDuration(),
                    queueLimit = LabsTestsUtils.randomLabQueueLimit(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory queue limit and name`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory queue limit and name
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    queueLimit = LabsTestsUtils.randomLabQueueLimit(),
                    name = LabsTestsUtils.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory with invalid labName (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid name
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = LabName(""),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labName (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid name
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = LabName("a".repeat(HttpUtils.labDomainConfig.maxLabNameLength + 1)),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDescription (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid description
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    description = LabDescription("a"),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDescription (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid description
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    description = LabDescription("a".repeat(HttpUtils.labDomainConfig.maxLabDescriptionLength + 1)),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDuration (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid duration
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.minLabDuration.minus(1.minutes)),
                        ),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDuration (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid duration
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.maxLabDuration.plus(1.minutes)),
                        ),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.minLabQueueLimit - 1),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.maxLabQueueLimit + 1),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            LabsTestsUtils.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }
    }
}
