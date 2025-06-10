package isel.rl.core.host

import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.utils.GroupsTestsUtils
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.LabsTestsUtils
import isel.rl.core.host.utils.UsersTestsUtils
import isel.rl.core.http.model.Problem
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test
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
    inner class CreateLaboratory {
        @Test
        fun `create laboratory with teacher user`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            LabsTestsUtils.createLab(testClient = testClient, authToken = user.authToken)
        }

        @Test
        fun `create laboratory with admin user`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient, Role.ADMIN)

            // when: creating a laboratory
            LabsTestsUtils.createLab(testClient = testClient, authToken = user.authToken)
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
        fun `create laboratory with invalid labName length (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    name = LabName(""),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    name = LabName("a".repeat(HttpUtils.labDomainConfig.maxLabNameLength + 1)),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    description = LabDescription("a"),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    description = LabDescription("a".repeat(HttpUtils.labDomainConfig.maxLabDescriptionLength + 1)),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.minLabDuration.minus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    duration =
                        LabDuration(
                            (HttpUtils.labDomainConfig.maxLabDuration.plus(1.minutes)),
                        ),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.minLabQueueLimit - 1),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
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
            val user = UsersTestsUtils.createUser(testClient)

            val invalidLaboratory =
                LabsTestsUtils.InitialLab(
                    queueLimit = LabQueueLimit(HttpUtils.labDomainConfig.maxLabQueueLimit + 1),
                )

            // when: creating a laboratory
            LabsTestsUtils.createLab(
                testClient,
                user.authToken,
                invalidLaboratory,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }
    }

    @Nested
    inner class LabRetrieval {
        @Test
        fun `get laboratory by id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, lab)
        }

        @Test
        fun `get laboratory by invalid id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(
                testClient,
                user.authToken,
                "invalid_id",
                Problem.invalidLaboratoryId,
                HttpStatus.BAD_REQUEST,
            )
        }

        @Test
        fun `get non existent laboratory by id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(
                testClient,
                user.authToken,
                "999999",
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `get user laboratories`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: retrieving the laboratories by userId with default limit and skip
            LabsTestsUtils.getLabsByUser(
                testClient,
                user.authToken,
                listOf(lab),
            )
        }

        @Test
        fun `get user laboratories (empty)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: retrieving the laboratories by userId with default limit and skip
            LabsTestsUtils.getLabsByUser(
                testClient,
                user.authToken,
                emptyList(),
            )
        }
    }

    @Nested
    inner class UpdateLaboratory {
        @Test
        fun `update laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

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

            // when: retrieving the laboratory by id and verify
            LabsTestsUtils.getLabById(testClient, user.authToken, updateLab)
        }

        @Test
        fun `update laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = UsersTestsUtils.createUser(testClient)
            val user2 = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(testClient, user2.authToken, updateLab, Problem.laboratoryNotFound)
        }

        @Test
        fun `update laboratory (null name)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory name
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    name = null,
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            val expectedLab = updateLab.copy(name = lab.name)

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, expectedLab)
        }

        @Test
        fun `update laboratory (null description)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory description
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    description = null,
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            val expectedLab = updateLab.copy(description = lab.description)

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, expectedLab)
        }

        @Test
        fun `update laboratory (null duration)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory duration
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    duration = null,
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            val expectedLab = updateLab.copy(duration = lab.duration)

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, expectedLab)
        }

        @Test
        fun `update laboratory (null queue limit)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: updating the laboratory queue limit
            val updateLab =
                LabsTestsUtils.InitialLab(
                    id = lab.id,
                    queueLimit = null,
                    createdAt = lab.createdAt,
                    ownerId = lab.ownerId,
                )

            val expectedLab = updateLab.copy(queueLimit = lab.queueLimit)

            // when: updating the laboratory
            LabsTestsUtils.updateLab(testClient, user.authToken, updateLab)

            // when: retrieving the laboratory by id
            LabsTestsUtils.getLabById(testClient, user.authToken, expectedLab)
        }

        @Test
        fun `update laboratory name and description`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

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
            val user = UsersTestsUtils.createUser(testClient)

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
            val user = UsersTestsUtils.createUser(testClient)

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
            val user = UsersTestsUtils.createUser(testClient)

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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
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
            val user = UsersTestsUtils.createUser(testClient)

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
            LabsTestsUtils.updateLab(
                testClient,
                user.authToken,
                updateLab,
                LabsTestsUtils.expectedInvalidLabQueueLimitProblem,
            )
        }
    }

    @Nested
    inner class LaboratoryDeletion {
        @Test
        fun `delete laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: deleting the laboratory
            LabsTestsUtils.deleteLab(testClient, user.authToken, lab.id.toString())

            // when: retrieving the laboratory by id and verify it does not exist
            LabsTestsUtils.getLabById(
                testClient,
                user.authToken,
                lab.id.toString(),
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `delete laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = UsersTestsUtils.createUser(testClient)
            val user2 = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: deleting the laboratory with another user
            LabsTestsUtils.deleteLab(
                testClient,
                user2.authToken,
                lab.id.toString(),
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `delete non existent laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: deleting the non existent laboratory
            LabsTestsUtils.deleteLab(
                testClient,
                user.authToken,
                "999999",
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }
    }

    @Nested
    inner class AddGroupToLaboratory {
        @Test
        fun `add group to laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory
            LabsTestsUtils.addGroupToLaboratory(testClient, user.authToken, lab.id, group.id)
        }

        /*
        @Test
        fun `add group to laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = UsersTestsUtils.createUser(testClient)
            val user2 = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory with another user
            LabsTestsUtils.addGroupToLab(
                testClient,
                user2.authToken,
                lab.id,
                group.id,
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `add group to laboratory with invalid labId`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory with an invalid labId
            LabsTestsUtils.addGroupToLab(
                testClient,
                user.authToken,
                "invalid_id",
                group.id,
                LabsTestsUtils.expectedInvalidLaboratoryIdProblem,
                HttpStatus.BAD_REQUEST,
            )
        }

         */
    }

    @Nested
    inner class RemoveGroupFromLaboratory {
        @Test
        fun `remove group from laboratory`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory
            LabsTestsUtils.addGroupToLaboratory(testClient, user.authToken, lab.id, group.id)

            // when: removing the group from the laboratory
            LabsTestsUtils.removeGroupFromLaboratory(testClient, user.authToken, lab.id, group.id)
        }

        /*
        @Test
        fun `remove group from laboratory not owned`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory and other user
            val user = UsersTestsUtils.createUser(testClient)
            val user2 = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory
            LabsTestsUtils.addGroupToLab(testClient, user.authToken, lab.id, group.id)

            // when: removing the group from the laboratory with another user
            LabsTestsUtils.removeGroupFromLab(
                testClient,
                user2.authToken,
                lab.id,
                group.id,
                Problem.laboratoryNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

         */
    }

    @Nested
    inner class GetLaboratoriesGroups {
        @Test
        fun `get laboratory groups`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user to be the owner of the laboratory
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating a laboratory
            val lab = LabsTestsUtils.createLab(testClient, authToken = user.authToken)

            // when: creating a group
            val group = GroupsTestsUtils.createGroup(testClient, authToken = user.authToken)

            // when: adding the group to the laboratory
            LabsTestsUtils.addGroupToLaboratory(testClient, user.authToken, lab.id, group.id)

            // when: retrieving the groups of the laboratory
            LabsTestsUtils.getLaboratoryGroups(testClient, user.authToken, lab.id, listOf(group))
        }
    }
}
