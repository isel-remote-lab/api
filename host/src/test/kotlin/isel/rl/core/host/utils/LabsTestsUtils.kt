package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.host.utils.HttpUtils.AUTH_HEADER_NAME
import isel.rl.core.host.utils.HttpUtils.assertProblem
import isel.rl.core.host.utils.HttpUtils.getBodyDataFromResponse
import isel.rl.core.host.utils.HttpUtils.labDomainConfig
import isel.rl.core.host.utils.HttpUtils.random
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import kotlinx.datetime.Instant
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object LabsTestsUtils {
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

        val lab = getBodyDataFromResponse<Map<*, *>>(response, "Laboratory created successfully")

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

        val lab = getBodyDataFromResponse<Map<*, *>>(response, "Laboratory found with the id ${expectedLab.id}")

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
