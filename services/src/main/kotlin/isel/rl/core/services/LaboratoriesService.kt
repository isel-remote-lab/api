package isel.rl.core.services

import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateLaboratoryResult
import isel.rl.core.services.interfaces.ILaboratoriesService
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
data class LaboratoriesService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val laboratoriesDomain: LaboratoriesDomain
): ILaboratoriesService {
    override fun createLaboratory(
        labName: String,
        labDescription: String,
        labDuration: Int,
        labQueue: Int,
        ownerId: Int,
    ): CreateLaboratoryResult =
        try {
            val laboratory =
                laboratoriesDomain.validateCreateLaboratory(
                    labName,
                    labDescription,
                    labDuration,
                    labQueue,
                    clock.now(),
                    ownerId
                )
            transactionManager.run {
                success(
                    it.laboratoriesRepository.createLaboratory(laboratory),
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }
}