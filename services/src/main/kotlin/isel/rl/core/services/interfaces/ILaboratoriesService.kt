package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.utils.Either

typealias CreateLaboratoryResult = Either<ServicesExceptions, Int>
typealias GetLaboratoryResult = Either<ServicesExceptions, Laboratory>
typealias UpdateLaboratoryResult = Either<ServicesExceptions, Unit>

interface ILaboratoriesService {
    fun createLaboratory(
        labName: String,
        labDescription: String,
        labDuration: Int,
        labQueueLimit: Int,
        ownerId: Int
    ): CreateLaboratoryResult

    fun getLaboratoryById(
        id: String
    ): GetLaboratoryResult

    fun updateLaboratory(
        labId: String,
        labName: String? = null,
        labDescription: String? = null,
        labDuration: Int? = null,
        labQueueLimit: Int? = null,
        userId: Int
    ): UpdateLaboratoryResult
}