package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.Either

typealias CreateLaboratoryResult = Either<ServicesExceptions, Int>

interface ILaboratoriesService {
    fun createLaboratory(
        labName: String,
        labDescription: String,
        labDuration: Int,
        labQueue: Int,
        ownerId: Int
    ): CreateLaboratoryResult
}