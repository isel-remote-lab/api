package isel.rl.core.services.interfaces

import EventEmitter
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.Either

typealias ValidateLabSessionCreationResult = Either<ServicesExceptions, Unit>

interface ILabSessionService {
    fun handleLabSessionCreation(
        labId: String,
        userId: Int,
        listener: EventEmitter,
    ): ValidateLabSessionCreationResult
}
