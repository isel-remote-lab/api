package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.host.utils.HttpUtilsTest
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
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient = testClient, authToken = user.authToken)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, lab)
        }

        @Test
        fun `get user laboratories (empty)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user
            val user = usersHelper.createTestUser(testClient)

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(HttpUtilsTest.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
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
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: retrieving the laboratories by userId with default limit and skip
            testClient
                .get()
                .uri(Uris.Laboratories.GET_ALL_BY_USER)
                .header(HttpUtilsTest.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
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
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user
            val user = usersHelper.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, "a")
                .header(HttpUtilsTest.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<Problem>()
                .consumeWith { HttpUtilsTest.assertProblem(Problem.invalidLaboratoryId, it) }
        }

        @Test
        fun `get non existent laboratory by id`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user
            val user = usersHelper.createTestUser(testClient)

            // when: retrieving the laboratory by id
            testClient
                .get()
                .uri(Uris.Laboratories.GET, 9999)
                .header(HttpUtilsTest.AUTH_HEADER_NAME, "Bearer ${user.authToken}")
                .exchange()
                .expectStatus().isNotFound
                .expectBody<Problem>()
                .consumeWith { HttpUtilsTest.assertProblem(Problem.laboratoryNotFound, it) }
        }

        @Test
        fun `create laboratory with invalid labName length (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    name = LabName(""),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedRequiredLabNameProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labName length (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    name = LabName("a".repeat(HttpUtilsTest.labDomainConfig.maxLabNameLength + 1)),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDescription length (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    description = LabDescription("a"),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDescription length (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    description = LabDescription("a".repeat(HttpUtilsTest.labDomainConfig.maxLabDescriptionLength + 1)),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDuration value (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtilsTest.labDomainConfig.minLabDuration.minus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labDuration value (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtilsTest.labDomainConfig.maxLabDuration.plus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtilsTest.labDomainConfig.minLabQueueLimit - 1),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabQueueLimitProblem,
            )
        }

        @Test
        fun `create laboratory with invalid labQueueLimit value (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            val invalidLaboratory =
                HttpUtilsTest.Laboratories.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtilsTest.labDomainConfig.maxLabQueueLimit + 1),
                )

            // when: creating a laboratory
            laboratoriesHelper.createInvalidLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                laboratoriesHelper.expectedInvalidLabQueueLimitProblem,
            )
        }
    }

    @Nested
    inner class UpdateLaboratoryTestsWithAuthHeader {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    createdAt = lab.createdAt,
                    ownerId = user.id,
                )
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id adn verify
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = usersHelper.createTestUser(testClient)
            val user2 = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    name = HttpUtilsTest.Laboratories.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(testClient, user2.authToken, updateLab, Problem.laboratoryNotFound)
        }

        @Test
        fun `update laboratory only name`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory name
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    name = laboratoriesHelper.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only description`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory description
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    description = HttpUtilsTest.Laboratories.newTestLabDescription(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only duration`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory duration
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    duration = HttpUtilsTest.Laboratories.newTestLabDuration(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory only queue limit`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory queue limit
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    queueLimit = HttpUtilsTest.Laboratories.randomLabQueueLimit(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory name and description
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    name = HttpUtilsTest.Laboratories.newTestLabName(),
                    description = HttpUtilsTest.Laboratories.newTestLabDescription(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory description and duration`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory description and duration
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    description = HttpUtilsTest.Laboratories.newTestLabDescription(),
                    duration = HttpUtilsTest.Laboratories.newTestLabDuration(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory duration and queue limit`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory duration and queue limit
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    duration = HttpUtilsTest.Laboratories.newTestLabDuration(),
                    queueLimit = HttpUtilsTest.Laboratories.randomLabQueueLimit(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory queue limit and name`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory queue limit and name
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    queueLimit = HttpUtilsTest.Laboratories.randomLabQueueLimit(),
                    name = HttpUtilsTest.Laboratories.newTestLabName(),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            laboratoriesHelper.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory with invalid labName (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid name
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    name = LabName(""),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labName (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid name
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    name = LabName("a".repeat(HttpUtilsTest.labDomainConfig.maxLabNameLength + 1)),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabNameProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDescription (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid description
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    description = LabDescription("a"),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDescription (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid description
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    description = LabDescription("a".repeat(HttpUtilsTest.labDomainConfig.maxLabDescriptionLength + 1)),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabDescriptionProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDuration (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid duration
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    duration =
                        LabDuration(
                            (HttpUtilsTest.labDomainConfig.minLabDuration.minus(1.minutes)),
                        ),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labDuration (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid duration
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    duration =
                        LabDuration(
                            (HttpUtilsTest.labDomainConfig.maxLabDuration.plus(1.minutes)),
                        ),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabDurationProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (min)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    queueLimit = LabQueueLimit(HttpUtilsTest.labDomainConfig.minLabQueueLimit - 1),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabQueueLimitProblem,
            )
        }

        @Test
        fun `update laboratory with invalid labQueueLimit (max)`() {
            // given: a test client
            val testClient = HttpUtilsTest.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = usersHelper.createTestUser(testClient)

            // when: creating a laboratory
            val lab = laboratoriesHelper.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory with invalid queue limit
            val updateLab =
                HttpUtilsTest.Laboratories.InitialLab(
                    id = lab.id,
                    queueLimit = LabQueueLimit(HttpUtilsTest.labDomainConfig.maxLabQueueLimit + 1),
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            // when: updating the laboratory
            laboratoriesHelper.updateInvalidLab(
                testClient,
                user.authToken,
                updateLab,
                laboratoriesHelper.expectedInvalidLabQueueLimitProblem,
            )
        }
    }

    companion object {
        private val laboratoriesHelper = HttpUtilsTest.Laboratories
        private val usersHelper = HttpUtilsTest.Users
    }
}
