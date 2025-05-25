package isel.rl.core.services.utils

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.repository.jdbi.transaction.JdbiTransactionManager
import isel.rl.core.services.LaboratoriesService
import isel.rl.core.services.TestClock
import isel.rl.core.services.utils.ServicesUtils.groupsDomain
import isel.rl.core.services.utils.ServicesUtils.jdbi
import isel.rl.core.services.utils.ServicesUtils.laboratoriesDomain
import isel.rl.core.services.utils.ServicesUtils.labsDomainConfig
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object LabsServicesUtils {
    fun createLabsServices(testClock: TestClock): LaboratoriesService =
        LaboratoriesService(
            JdbiTransactionManager(jdbi),
            testClock,
            laboratoriesDomain,
            groupsDomain,
        )

    data class InitialLab(
        val id: Int = 0,
        val name: String? = newTestLabName(),
        val description: String? = newTestLabDescription(),
        val duration: Int? = newTestLabDuration(),
        val queueLimit: Int? = randomLabQueueLimit(),
        val createdAt: Instant = Instant.DISTANT_PAST,
        // Default owner ID for testing
        val ownerId: Int = 1,
    )

    fun createLab(
        labsService: LaboratoriesService,
        initialLab: InitialLab = InitialLab(),
        owner: User = User(initialLab.ownerId, Role.TEACHER, Name(""), Email(""), Instant.DISTANT_PAST),
        expectedServiceException: KClass<*>? = null,
    ): InitialLab {
        val lab =
            labsService.createLaboratory(
                initialLab.name,
                initialLab.description,
                initialLab.duration,
                initialLab.queueLimit,
                owner,
            )

        if (expectedServiceException != null) {
            assertTrue(lab is Failure, "Expected a failure, but got: $lab")
            assertTrue(
                expectedServiceException.isInstance(lab.value),
                "Expected a ServiceException, but got: ${lab.value}",
            )
            return initialLab
        }

        val expectedLabName = if (initialLab.name.isNullOrBlank()) null else initialLab.name
        val expectedLabDescription = if (initialLab.description.isNullOrBlank()) null else initialLab.description
        val expectedLabQueueLimit = if (initialLab.queueLimit == null) null else initialLab.queueLimit.toInt()

        assertTrue(lab is Success, "Expected a success, but got: $lab")
        assertEquals(expectedLabName, lab.value.labName.labNameInfo, "Lab name does not match")
        assertEquals(
            expectedLabDescription,
            lab.value.labDescription.labDescriptionInfo,
            "Lab description does not match",
        )
        assertEquals(
            initialLab.duration,
            lab.value.labDuration.labDurationInfo!!.inWholeMinutes.toInt(),
            "Lab duration does not match",
        )
        assertEquals(expectedLabQueueLimit, lab.value.labQueueLimit.labQueueLimitInfo, "Lab queue limit does not match")
        return initialLab.copy(
            id = lab.value.id,
            createdAt = lab.value.createdAt,
        )
    }

    fun getLabById(
        labsService: LaboratoriesService,
        userId: Int,
        expectedLab: InitialLab,
    ): InitialLab {
        val lab = labsService.getLaboratoryById(expectedLab.id.toString(), userId)

        assertTrue(lab is Success, "Expected a successful lab retrieval, but got: $lab")
        assertLab(expectedLab, lab.value)
        return expectedLab.copy(
            id = lab.value.id,
            createdAt = lab.value.createdAt,
        )
    }

    fun getLabById(
        labsService: LaboratoriesService,
        labId: String,
        userId: Int,
        expectedServiceException: KClass<*>,
    ) {
        val lab = labsService.getLaboratoryById(labId, userId)

        assertTrue(lab is Failure, "Expected a failure, but got: $lab")
        assertTrue(
            expectedServiceException.isInstance(lab.value),
            "Expected a ServiceException, but got: ${lab.value}",
        )
    }

    fun getUserLabs(
        labsService: LaboratoriesService,
        userId: Int,
        limit: String? = null,
        skip: String? = null,
        expectedLabs: List<InitialLab> = emptyList(),
        expectedServiceException: KClass<*>? = null,
    ) {
        val labs = labsService.getAllLaboratoriesByUser(userId, limit, skip)

        if (expectedServiceException != null) {
            assertTrue(labs is Failure, "Expected a failure, but got: $labs")
            assertTrue(
                expectedServiceException.isInstance(labs.value),
                "Expected a ServiceException, but got: ${labs.value}",
            )
            return
        }

        assertTrue(labs is Success, "Expected a successful lab retrieval, but got: $labs")
        assertEquals(expectedLabs.size, labs.value.size, "Number of labs does not match expected size.")

        expectedLabs.forEach { expectedLab ->
            assertTrue(
                labs.value.any { it.id == expectedLab.id },
                "Expected lab with ID ${expectedLab.id} not found in user labs.",
            )
        }
    }

    fun updateLab(
        labsService: LaboratoriesService,
        initialLab: InitialLab,
        expectedLab: InitialLab =
            InitialLab(
                id = initialLab.id,
                name = newTestLabName(),
                description = newTestLabDescription(),
                duration = newTestLabDuration(),
                queueLimit = randomLabQueueLimit(),
                createdAt = initialLab.createdAt,
                ownerId = initialLab.ownerId,
            ),
    ) {
        val updatedLab =
            labsService.updateLaboratory(
                initialLab.id.toString(),
                expectedLab.name,
                expectedLab.description,
                expectedLab.duration,
                expectedLab.queueLimit,
                initialLab.ownerId,
            )

        assertTrue(updatedLab is Success, "Expected a successful lab update, but got: $updatedLab")
        getLabById(
            labsService,
            initialLab.ownerId,
            expectedLab.copy(
                name = expectedLab.name ?: initialLab.name,
                description = expectedLab.description ?: initialLab.description,
                duration = expectedLab.duration ?: initialLab.duration,
                queueLimit = expectedLab.queueLimit ?: initialLab.queueLimit,
            ),
        )
    }

    fun updateLab(
        labsService: LaboratoriesService,
        labId: String,
        updateLab: InitialLab = InitialLab(),
        expectedServiceException: KClass<*>,
    ) {
        val updatedLab =
            labsService.updateLaboratory(
                labId,
                updateLab.name,
                updateLab.description,
                updateLab.duration,
                updateLab.queueLimit,
                updateLab.ownerId,
            )

        assertTrue(updatedLab is Failure, "Expected a failure, but got: $updatedLab")
        assertTrue(
            expectedServiceException.isInstance(updatedLab.value),
            "Expected a ServiceException , but got: ${updatedLab.value}",
        )
    }

    fun addGroupToLab(
        labsService: LaboratoriesService,
        labId: String,
        groupId: String?,
        ownerId: Int,
        expectedServiceException: KClass<*>? = null,
    ) {
        val addGroupResult = labsService.addGroupToLaboratory(labId, groupId, ownerId)

        if (expectedServiceException != null) {
            assertTrue(addGroupResult is Failure, "Expected a failure, but got: $addGroupResult")
            assertTrue(
                expectedServiceException.isInstance(addGroupResult.value),
                "Expected a ServiceException, but got: ${addGroupResult.value}",
            )
            return
        }

        assertTrue(addGroupResult is Success, "Expected a successful group addition, but got: $addGroupResult")
    }

    fun deleteLab(
        labsService: LaboratoriesService,
        labId: String,
        userId: Int,
        expectedServiceException: KClass<*>? = null,
    ) {
        val deleteResult = labsService.deleteLaboratory(labId, userId)

        if (expectedServiceException != null) {
            assertTrue(deleteResult is Failure, "Expected a failure, but got: $deleteResult")
            assertTrue(
                expectedServiceException.isInstance(deleteResult.value),
                "Expected a ServiceException, but got: ${deleteResult.value}",
            )
            return
        }

        assertTrue(deleteResult is Success, "Expected a successful lab deletion, but got: $deleteResult")
        getLabById(labsService, labId, userId, ServicesExceptions.Laboratories.LaboratoryNotFound::class)
    }

    fun newTestLabName() = "lab-${abs(Random.nextLong())}"

    fun newTestInvalidLabNameMax() = "a".repeat(labsDomainConfig.maxLabNameLength + 1)

    fun newTestInvalidLabNameMin() = ""

    val isLabNameOptional = labsDomainConfig.isLabNameOptional

    fun newTestLabDescription() = "description-${abs(Random.nextLong())}"

    fun newTestInvalidLabDescriptionMax() = "a".repeat(labsDomainConfig.maxLabDescriptionLength + 1)

    fun newTestInvalidLabDescriptionMin() = ""

    val isLabDescriptionOptional = labsDomainConfig.isLabDescriptionOptional

    fun newTestLabDuration() = (labsDomainConfig.minLabDuration..labsDomainConfig.maxLabDuration).random().inWholeMinutes.toInt()

    fun newTestInvalidLabDurationMax() = (labsDomainConfig.maxLabDuration.inWholeMinutes + 1).toInt()

    fun newTestInvalidLabDurationMin() = (labsDomainConfig.minLabDuration.inWholeMinutes - 1).toInt()

    val isLabDurationOptional = labsDomainConfig.isLabDurationOptional

    fun randomLabQueueLimit() = (labsDomainConfig.minLabQueueLimit..labsDomainConfig.maxLabQueueLimit).random()

    fun newTestInvalidLabQueueLimitMax() = labsDomainConfig.maxLabQueueLimit + 1

    fun newTestInvalidLabQueueLimitMin() = labsDomainConfig.minLabQueueLimit - 1

    val isLabQueueLimitOptional = labsDomainConfig.isLabQueueLimitOptional

    fun assertLab(
        expectedLab: InitialLab,
        actualLab: Laboratory,
    ) {
        assertEquals(expectedLab.id, actualLab.id, "Lab ID does not match expected value.")
        assertEquals(expectedLab.name, actualLab.labName.labNameInfo, "Lab name does not match expected value.")
        assertEquals(
            expectedLab.description,
            actualLab.labDescription.labDescriptionInfo,
            "Lab description does not match expected value.",
        )
        assertEquals(
            expectedLab.duration,
            actualLab.labDuration.labDurationInfo?.inWholeMinutes?.toInt(),
            "Lab duration does not match expected value.",
        )
        assertEquals(
            expectedLab.queueLimit,
            actualLab.labQueueLimit.labQueueLimitInfo,
            "Lab queue limit does not match expected value.",
        )
        assertEquals(expectedLab.createdAt, actualLab.createdAt, "CreatedAt does not match expected value.")
    }

    /**
     * Returns a random Duration between start (inclusive) and endInclusive (inclusive).
     */
    private fun ClosedRange<Duration>.random(random: Random = Random.Default): Duration {
        val minNanos = start.inWholeMinutes
        val maxNanos = endInclusive.inWholeMinutes
        require(maxNanos >= minNanos) { "Invalid Duration range: $start..$endInclusive" }

        val randomNanos = random.nextLong(minNanos, maxNanos + 1)
        return randomNanos.minutes
    }
}
