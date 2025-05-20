package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateLaboratoryResult
import isel.rl.core.services.interfaces.GetAllLaboratoriesResult
import isel.rl.core.services.interfaces.GetLaboratoryResult
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.services.interfaces.UpdateLaboratoryResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.services.utils.verifyQuery
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

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
) : ILaboratoriesService {
    override fun createLaboratory(
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        ownerId: Int,
    ): CreateLaboratoryResult =
        try {
            // Validate the laboratory data
            val laboratory =
                laboratoriesDomain.validateCreateLaboratory(
                    labName = labName,
                    labDescription = labDescription,
                    labDuration = labDuration,
                    labQueueLimit = labQueueLimit,
                    createdAt = clock.now(),
                    ownerId = ownerId,
                )
            transactionManager.run {
                // Create the laboratory in the database and return the result as success
                val labId = it.laboratoriesRepository.createLaboratory(laboratory)
                success(
                    Laboratory(
                        labId,
                        laboratory.labName,
                        laboratory.labDescription,
                        laboratory.labDuration,
                        laboratory.labQueueLimit,
                        laboratory.createdAt,
                        ownerId,
                    ),
                )
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the creation process
            handleException(e)
        }

    override fun getLaboratoryById(
        id: String,
        userId: Int,
    ): GetLaboratoryResult =
        try {
            // Validate the laboratory ID
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(id)

            transactionManager.run {
                val laboratoriesRepo = it.laboratoriesRepository

                // Retrieve the laboratory by ID from the database and return the result as success
                // If the laboratory is not found, return failure with LaboratoryNotFound exception
                laboratoriesRepo.getLaboratoryById(validatedLabId)
                    ?.let { laboratory ->
                        // Check if the user belongs to the laboratory or is the owner
                        if (!laboratoriesRepo.checkIfUserBelongsToLaboratory(validatedLabId, userId)) {
                            // For security reasons, we don't want to expose the existence of a laboratory
                            return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                        } else {
                            success(laboratory)
                        }
                    }
                    ?: failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the retrieval process
            handleException(e)
        }

    override fun updateLaboratory(
        labId: String,
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        ownerId: Int,
    ): UpdateLaboratoryResult =
        try {
            // Validate received parameters
            // Since validate methods of domain verify if the parameters are null or empty,
            // a let is used to avoid the null verification
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)
            val validatedLabName = labName?.let { laboratoriesDomain.validateLaboratoryName(labName) }
            val validatedLabDescription =
                labDescription?.let { laboratoriesDomain.validateLabDescription(labDescription) }
            val validatedLabDuration = labDuration?.let { laboratoriesDomain.validateLabDuration(labDuration) }
            val validatedLabQueueLimit = labQueueLimit?.let { laboratoriesDomain.validateLabQueueLimit(labQueueLimit) }

            transactionManager.run {
                val laboratoriesRepo = it.laboratoriesRepository

                // Check if the laboratory exists
                if (!laboratoriesRepo.checkIfLaboratoryExists(validatedLabId)) {
                    return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                }

                // Check if the laboratory belongs to the user
                if (laboratoriesRepo.getLaboratoryOwnerId(validatedLabId) != ownerId) {
                    return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
                }

                // If update is successful, return success
                // Otherwise, return failure with unexpected error
                if (laboratoriesRepo.updateLaboratory(
                        validatedLabId,
                        validatedLabName,
                        validatedLabDescription,
                        validatedLabDuration,
                        validatedLabQueueLimit,
                    )
                ) {
                    success(Unit)
                } else {
                    failure(ServicesExceptions.UnexpectedError)
                }
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the update process
            handleException(e)
        }

    override fun getAllLaboratoriesByUser(
        userId: Int,
        limit: String?,
        skip: String?,
    ): GetAllLaboratoriesResult =
        try {
            // Validate limit and skip
            val limitAndSkip = verifyQuery(limit, skip)

            transactionManager.run {
                // Retrieve all laboratories by user ID from the database and return the result as success
                success(
                    it.laboratoriesRepository
                        .getLaboratoriesByUserId(userId, limitAndSkip),
                )
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the retrieval process
            handleException(e)
        }
}
