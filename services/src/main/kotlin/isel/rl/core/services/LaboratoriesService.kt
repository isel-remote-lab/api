package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateLaboratoryResult
import isel.rl.core.services.interfaces.GetLaboratoryResult
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.services.interfaces.UpdateLaboratoryResult
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
data class LaboratoriesService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val laboratoriesDomain: LaboratoriesDomain
) : ILaboratoriesService {
    override fun createLaboratory(
        labName: String,
        labDescription: String,
        labDuration: Int,
        labQueueLimit: Int,
        ownerId: Int,
    ): CreateLaboratoryResult =
        try {
            val laboratory =
                laboratoriesDomain.validateCreateLaboratory(
                    labName = labName,
                    labDescription = labDescription,
                    labDuration = labDuration,
                    labQueueLimit = labQueueLimit,
                    createdAt = clock.now(),
                    ownerId = ownerId
                )
            transactionManager.run {
                success(
                    it.laboratoriesRepository.createLaboratory(laboratory),
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }

    override fun getLaboratoryById(id: String): GetLaboratoryResult =
        try {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(id)

            transactionManager.run {
                it.laboratoriesRepository.getLaboratoryById(validatedLabId)
                    ?.let(::success)
                    ?: failure(ServicesExceptions.Laboratories.LaboratoryNotFound)
            }
        } catch (e: Exception) {
            handleException(e)
        }

    override fun updateLaboratory(
        labId: String,
        labName: String?,
        labDescription: String?,
        labDuration: Int?,
        labQueueLimit: Int?,
        userId: Int
    ): UpdateLaboratoryResult =
        try {
            val validatedLabId = laboratoriesDomain.validateLaboratoryId(labId)

            transactionManager.run {
                val laboratoriesRepo = it.laboratoriesRepository

                if (!laboratoriesRepo.checkIfLaboratoryExists(validatedLabId))
                    return@run failure(ServicesExceptions.Laboratories.LaboratoryNotFound)

                // Check if the laboratory belongs to the user
                val ownerId = laboratoriesRepo.getLaboratoryOwnerId(validatedLabId)
                if (ownerId != userId)
                    return@run failure(ServicesExceptions.Laboratories.LaboratoryNotOwned)

                val validatedUpdateLaboratory = laboratoriesDomain.validateUpdateLaboratory(
                    labId = validatedLabId,
                    labName = labName,
                    labDescription = labDescription,
                    labDuration = labDuration,
                    labQueueLimit = labQueueLimit
                )

                // If update is successful, return success
                // Otherwise, return failure with unexpected error
                if (laboratoriesRepo.updateLaboratory(validatedUpdateLaboratory))
                    success(Unit)
                else
                    failure(ServicesExceptions.UnexpectedError)

            }
        } catch (e: Exception) {
            handleException(e)
        }
}