package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.hardware.domain.HardwareDomain
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.user.User
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.AddGroupToLaboratoryResult
import isel.rl.core.services.interfaces.AddHardwareToLaboratoryResult
import isel.rl.core.services.interfaces.CreateLaboratoryResult
import isel.rl.core.services.interfaces.DeleteLaboratoryResult
import isel.rl.core.services.interfaces.GetAllLaboratoriesResult
import isel.rl.core.services.interfaces.GetLaboratoryGroupsResult
import isel.rl.core.services.interfaces.GetLaboratoryResult
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.services.interfaces.RemoveGroupFromLaboratoryResult
import isel.rl.core.services.interfaces.RemoveHardwareFromLaboratoryResult
import isel.rl.core.services.interfaces.UpdateLaboratoryResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.services.utils.verifyQuery
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
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
            handleException(e as Exception)
        }

    override fun getLaboratoryById(
        id: String,
        userId: Int,
    ): GetLaboratoryResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(id)
            transactionManager.run {
                val laboratoriesRepo = it.laboratoriesRepository

                laboratoriesRepo.getLaboratoryById(validatedLabId)?.takeIf {
                    laboratoriesRepo.checkIfUserBelongsToLaboratory(validatedLabId, userId)
                }?.let { lab -> success(lab) }
                    ?: failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
            }
        }.getOrElse { e ->
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
            // Validate received parameters
            // Since validate methods of domain verify if the parameters are null or empty,
            // a let is used to avoid the null verification
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedLabName = labName?.let(laboratoriesDomain::validateLaboratoryName)
            val validatedLabDescription = labDescription?.let(laboratoriesDomain::validateLabDescription)
            val validatedLabDuration = labDuration?.let(laboratoriesDomain::validateLabDuration)
            val validatedLabQueueLimit = labQueueLimit?.let(laboratoriesDomain::validateLabQueueLimit)

            transactionManager.run {
                val labRepo = it.laboratoriesRepository

                // Check if the laboratory exists or if the owner ID matches
                if (!labRepo.checkIfLaboratoryExists(validatedLabId) || labRepo.getLaboratoryOwnerId(validatedLabId) != ownerId) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    // If update is successful, return success
                    // Otherwise, return failure with unexpected error
                    val updated =
                        labRepo.updateLaboratory(
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
            handleException(e as Exception)
        }

    override fun getLaboratoryGroups(
        labId: String,
        limit: String?,
        skip: String?,
    ): GetLaboratoryGroupsResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val limitAndSkip = verifyQuery(limit, skip)

            transactionManager.run {
                val labRepo = it.laboratoriesRepository
                val groupRepo = it.groupsRepository

                if (!labRepo.checkIfLaboratoryExists(validatedLabId)) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    // Get the groups associated with the laboratory
                    val groups =
                        labRepo.getLaboratoryGroups(validatedLabId, limitAndSkip).map { groupId ->
                            groupRepo.getGroupById(groupId)!!
                        }
                    success(groups)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun addGroupToLaboratory(
        labId: String,
        groupId: String?,
        ownerId: Int,
    ): AddGroupToLaboratoryResult =
        runCatching {
            // Validate the laboratory and group ID's
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedGroupId =
                groupId?.let(groupsDomain::validateGroupId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("GroupId cannot be null"))

            transactionManager.run {
                val labRepo = it.laboratoriesRepository

                return@run when {
                    !it.groupsRepository.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    !labRepo.checkIfLaboratoryExists(validatedLabId) ||
                        labRepo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(
                            ServicesExceptions.Laboratories.LaboratoryNotFound,
                        )

                    labRepo.addGroupToLaboratory(validatedLabId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun removeGroupFromLaboratory(
        labId: String,
        groupId: String?,
        ownerId: Int,
    ): RemoveGroupFromLaboratoryResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedGroupId =
                groupId?.let(groupsDomain::validateGroupId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("GroupId cannot be null"))

            transactionManager.run {
                val labRepo = it.laboratoriesRepository

                return@run when {
                    !it.groupsRepository.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    !labRepo.checkIfLaboratoryExists(validatedLabId) ||
                        labRepo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(
                            ServicesExceptions.Laboratories.LaboratoryNotFound,
                        )

                    labRepo.removeGroupFromLaboratory(validatedLabId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun addHardwareToLaboratory(
        labId: String,
        hardwareId: String?,
        ownerId: Int,
    ): AddHardwareToLaboratoryResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedHardwareId =
                hardwareId?.let(hardwareDomain::validateHardwareId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("HardwareId cannot be null"))

            transactionManager.run {
                val labRepo = it.laboratoriesRepository
                val hardwareRepo = it.hardwareRepository

                return@run when {
                    !hardwareRepo.checkIfHardwareExists(validatedHardwareId) -> failure(ServicesExceptions.Hardware.HardwareNotFound)
                    !labRepo.checkIfLaboratoryExists(validatedLabId) ||
                        labRepo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(
                            ServicesExceptions.Laboratories.LaboratoryNotFound,
                        )

                    labRepo.addHardwareToLaboratory(validatedLabId, validatedHardwareId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun removeHardwareFromLaboratory(
        labId: String,
        hardwareId: String?,
        ownerId: Int,
    ): RemoveHardwareFromLaboratoryResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedHardwareId =
                hardwareId?.let(hardwareDomain::validateHardwareId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("HardwareId cannot be null"))

            transactionManager.run {
                val labRepo = it.laboratoriesRepository
                val hardwareRepo = it.hardwareRepository

                return@run when {
                    !hardwareRepo.checkIfHardwareExists(validatedHardwareId) -> failure(ServicesExceptions.Hardware.HardwareNotFound)
                    !labRepo.checkIfLaboratoryExists(validatedLabId) ||
                        labRepo.getLaboratoryOwnerId(validatedLabId) != ownerId ->
                        failure(
                            ServicesExceptions.Laboratories.LaboratoryNotFound,
                        )

                    labRepo.removeHardwareLaboratory(validatedLabId, validatedHardwareId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun getAllLaboratoriesByUser(
        userId: Int,
        limit: String?,
        skip: String?,
    ): GetAllLaboratoriesResult =
        runCatching {
            val limitAndSkip = verifyQuery(limit, skip)
            transactionManager.run {
                success(it.laboratoriesRepository.getLaboratoriesByUserId(userId, limitAndSkip))
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun deleteLaboratory(
        labId: String,
        ownerId: Int,
    ): DeleteLaboratoryResult =
        runCatching {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)

            transactionManager.run {
                val repo = it.laboratoriesRepository

                if (!repo.checkIfLaboratoryExists(validatedLabId) || repo.getLaboratoryOwnerId(validatedLabId) != ownerId) {
                    failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                } else {
                    // Remove all groups associated with the laboratory before deleting it
                    repo.getLaboratoryGroups(validatedLabId).forEach { groupId ->
                        repo.removeGroupFromLaboratory(validatedLabId, groupId)
                    }

                    if (repo.deleteLaboratory(validatedLabId)) {
                        success(Unit)
                    } else {
                        failure(ServicesExceptions.UnexpectedError)
                    }
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }
}
