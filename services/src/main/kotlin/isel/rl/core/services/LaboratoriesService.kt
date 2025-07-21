package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.hardware.domain.HardwareDomain
import isel.rl.core.domain.hardware.props.HardwareStatus
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.user.User
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.AddGroupToLaboratoryResult
import isel.rl.core.services.interfaces.AddHardwareToLaboratoryResult
import isel.rl.core.services.interfaces.CreateLaboratoryResult
import isel.rl.core.services.interfaces.DeleteLaboratoryResult
import isel.rl.core.services.interfaces.GetAllLaboratoriesResult
import isel.rl.core.services.interfaces.GetLaboratoryGroupsResult
import isel.rl.core.services.interfaces.GetLaboratoryHardwareResult
import isel.rl.core.services.interfaces.GetLaboratoryResult
import isel.rl.core.services.interfaces.ILabWaitingQueueService
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.services.interfaces.RemoveGroupFromLaboratoryResult
import isel.rl.core.services.interfaces.RemoveHardwareFromLaboratoryResult
import isel.rl.core.services.interfaces.UpdateLaboratoryResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.services.utils.verifyQuery
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// runCatching is used to handle exceptions that may occur during the creation of a laboratory.
// It follows the functional programming paradigm, allowing for a more concise and readable code.
// Also it allows us to return a failure result in case of an exception, allowing to encapsulate the error handling logic.
// Try/catch breaks the code flow and it is not possible to encapsulate the error handling logic in a single place.

/**
 * Service class for managing laboratories.
 * This class provides methods to create, update, and retrieve laboratories from the database.
 * It uses the LaboratoriesDomain class for validation and the TransactionManager for database transactions.
 *
 * @property transactionManager The TransactionManager instance for managing database transactions.
 * @property clock The Clock instance for getting the current time.
 * @property laboratoriesDomain The LaboratoriesDomain instance for validating laboratory data.
 */
@Service
data class LaboratoriesService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val laboratoryWaitingQueueService: ILabWaitingQueueService,
    private val laboratoriesDomain: LaboratoriesDomain,
    private val groupsDomain: GroupsDomain,
    private val hardwareDomain: HardwareDomain,
) : ILaboratoriesService {
    override fun createLaboratory(
        name: String?,
        description: String?,
        duration: Int?,
        queueLimit: Int?,
        owner: User,
    ): CreateLaboratoryResult =
        runCatching {
            LOG.info("Creating laboratory with name: {}", name)
            val laboratory =
                laboratoriesDomain.validateCreateLaboratory(
                    name,
                    description,
                    duration,
                    queueLimit,
                    clock.now(),
                    owner.id,
                )
            transactionManager.run {
                val labId = it.laboratoriesRepository.createLaboratory(laboratory)
                success(laboratory.copy(id = labId, ownerId = owner.id))
            }
        }.getOrElse { e ->
            LOG.error("Error creating laboratory with name {}", name, e)
            handleException(e as Exception)
        }

    override fun getLaboratoryById(
        id: String,
        userId: Int,
    ): GetLaboratoryResult =
        runCatching {
            LOG.info("Getting laboratory with id: {}", id)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(id)

            transactionManager.run {
                val repo = it.laboratoriesRepository

                repo.getLaboratoryById(validatedLabId)?.takeIf {
                    repo.checkIfUserBelongsToLaboratory(validatedLabId, userId)
                }?.let { lab -> success(lab) }
                    ?: failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
            }
        }.getOrElse { e ->
            LOG.error("Error getting laboratory with id: {}", id, e)
            handleException(e as Exception)
        }

    override fun updateLaboratory(
        labId: String,
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        ownerId: Int,
    ): UpdateLaboratoryResult =
        runCatching {
            LOG.info("Updating laboratory with id: {}", labId)
            // Validate received parameters
            // Since validate methods of domain verify if the parameters are null or empty,
            // a let is used to avoid the null verification
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedLabName = labName?.let(laboratoriesDomain::validateLaboratoryName)
            val validatedLabDescription = labDescription?.let(laboratoriesDomain::validateLabDescription)
            val validatedLabDuration = labDuration?.let(laboratoriesDomain::validateLabDuration)
            val validatedLabQueueLimit = labQueueLimit?.let(laboratoriesDomain::validateLabQueueLimit)

            transactionManager.run {
                val repo = it.laboratoriesRepository

                // Check if the laboratory exists or if the owner ID matches
                if (!repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    // If update is successful, return success
                    // Otherwise, return failure with unexpected error
                    val updated =
                        repo.updateLaboratory(
                            validatedLabId,
                            validatedLabName,
                            validatedLabDescription,
                            validatedLabDuration,
                            validatedLabQueueLimit,
                        )
                    if (updated) success(Unit) else failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error("Error updating laboratory with id: {}, owner id: {}. Error: {}", labId, ownerId, e.message, e)
            handleException(e as Exception)
        }

    override fun getLaboratoryGroups(
        labId: String,
        limit: String?,
        skip: String?,
    ): GetLaboratoryGroupsResult =
        runCatching {
            LOG.info("Getting groups for laboratory with id: {}", labId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val limitAndSkip = verifyQuery(limit, skip)

            transactionManager.run {
                val labRepo = it.laboratoriesRepository
                val groupRepo = it.groupsRepository

                if (!labRepo.checkIfLaboratoryExists(validatedLabId)) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    val groups =
                        labRepo.getLaboratoryGroups(validatedLabId, limitAndSkip).mapNotNull { groupId ->
                            groupRepo.getGroupById(groupId)
                        }
                    success(groups)
                }
            }
        }.getOrElse { e ->
            LOG.error("Error getting groups for laboratory with id: {}. Error: {}", labId, e.message, e)
            handleException(e as Exception)
        }

    override fun addGroupToLaboratory(
        labId: String,
        groupId: String,
        ownerId: Int,
    ): AddGroupToLaboratoryResult =
        runCatching {
            LOG.info("Adding group to laboratory with id: {}", labId)
            // Validate the laboratory and group ID's
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedGroupId = groupsDomain.validateGroupId(groupId)

            transactionManager.run {
                val repo = it.laboratoriesRepository
                when {
                    !it.groupsRepository.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    !repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                    repo.checkIfGroupBelongsToLaboratory(
                        validatedLabId,
                        validatedGroupId,
                    ) -> failure(ServicesExceptions.Laboratories.GroupAlreadyInLaboratory)

                    repo.addGroupToLaboratory(validatedLabId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Error adding group with id: {} to laboratory with id: {}, owner id: {}. Error: {}",
                groupId,
                labId,
                ownerId,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun removeGroupFromLaboratory(
        labId: String,
        groupId: String,
        ownerId: Int,
    ): RemoveGroupFromLaboratoryResult =
        runCatching {
            LOG.info("Removing group from laboratory with id: {}", labId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedGroupId = groupsDomain.validateGroupId(groupId)

            transactionManager.run {
                val repo = it.laboratoriesRepository
                when {
                    !it.groupsRepository.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    !repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                    !repo.checkIfGroupBelongsToLaboratory(validatedLabId, validatedGroupId) ->
                        failure(ServicesExceptions.Laboratories.GroupNotFoundInLaboratory)

                    repo.removeGroupFromLaboratory(validatedLabId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Error removing group with id: {} from laboratory with id: {}, owner id: {}. Error: {}",
                groupId,
                labId,
                ownerId,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun getLaboratoryHardware(
        labId: String,
        limit: String?,
        skip: String?,
    ): GetLaboratoryHardwareResult =
        runCatching {
            LOG.info("Getting hardware for laboratory with id: {}", labId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val limitAndSkip = verifyQuery(limit, skip)

            transactionManager.run {
                val repo = it.laboratoriesRepository

                if (!repo.checkIfLaboratoryExists(validatedLabId)) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    success(
                        repo.getLaboratoryHardware(validatedLabId, limitAndSkip).map { hwId ->
                            it.hardwareRepository.getHardwareById(hwId)!!
                        },
                    )
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Error getting hardware for laboratory with id: {}. Error: {}",
                labId,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun addHardwareToLaboratory(
        labId: String,
        hardwareId: String,
        ownerId: Int,
    ): AddHardwareToLaboratoryResult =
        runCatching {
            LOG.info("Adding hardware to laboratory with id: {}", labId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedHardwareId = hardwareDomain.validateHardwareId(hardwareId)

            transactionManager.run {
                val repo = it.laboratoriesRepository
                when {
                    !it.hardwareRepository.checkIfHardwareExists(
                        validatedHardwareId,
                    ) -> failure(ServicesExceptions.Hardware.HardwareNotFound)

                    !repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                    repo.checkIfHardwareBelongsToLaboratory(validatedLabId, validatedHardwareId) ->
                        failure(
                            ServicesExceptions.Laboratories.HardwareAlreadyInLaboratory,
                        )

                    repo.addHardwareToLaboratory(validatedLabId, validatedHardwareId) -> {
                        if (it.hardwareRepository.getHardwareById(validatedHardwareId)?.status == HardwareStatus.Available
                            && !it.labWaitingQueueRepository.isLabQueueEmpty(
                                validatedLabId
                            )
                        ) {
                            laboratoryWaitingQueueService.popUserFromQueue(validatedLabId)
                        }

                        success(Unit)
                    }

                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Error adding hardware with id: {} to laboratory with id: {}, owner id: {}. Error: {}",
                hardwareId,
                labId,
                ownerId,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun removeHardwareFromLaboratory(
        labId: String,
        hardwareId: String,
        ownerId: Int,
    ): RemoveHardwareFromLaboratoryResult =
        runCatching {
            LOG.info("Removing hardware from laboratory with id: {}", labId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedHardwareId = hardwareDomain.validateHardwareId(hardwareId)

            transactionManager.run {
                val repo = it.laboratoriesRepository
                when {
                    !it.hardwareRepository.checkIfHardwareExists(
                        validatedHardwareId,
                    ) -> failure(ServicesExceptions.Hardware.HardwareNotFound)

                    !repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                    !repo.checkIfHardwareBelongsToLaboratory(validatedLabId, validatedHardwareId) ->
                        failure(ServicesExceptions.Laboratories.HardwareNotFoundInLaboratory)

                    repo.removeHardwareLaboratory(validatedLabId, validatedHardwareId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Error removing hardware with id: {} from laboratory with id: {}, owner id: {}. Error: {}",
                hardwareId,
                labId,
                ownerId,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun getAllLaboratoriesByUser(
        userId: Int,
        limit: String?,
        skip: String?,
    ): GetAllLaboratoriesResult =
        runCatching {
            LOG.info("Getting all laboratories for user with id: {}", userId)
            val limitAndSkip = verifyQuery(limit, skip)

            transactionManager.run {
                success(it.laboratoriesRepository.getLaboratoriesByUserId(userId, limitAndSkip))
            }
        }.getOrElse { e ->
            LOG.error("Error getting all laboratories for user with id: {}. Error: {}", userId, e.message, e)
            handleException(e as Exception)
        }

    override fun deleteLaboratory(
        labId: String,
        ownerId: Int,
    ): DeleteLaboratoryResult =
        runCatching {
            LOG.info("Deleting laboratory with id: {}, owner id: {}", labId, ownerId)
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)

            transactionManager.run {
                val repo = it.laboratoriesRepository
                if (!repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    repo.getLaboratoryGroups(validatedLabId)
                        .forEach { repo.removeGroupFromLaboratory(validatedLabId, it) }
                    if (repo.deleteLaboratory(validatedLabId)) success(Unit) else failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error("Error deleting laboratory with id: {}, owner id: {}. Error: {}", labId, ownerId, e.message, e)
            handleException(e as Exception)
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(LaboratoriesService::class.java)
    }
}
