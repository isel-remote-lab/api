package isel.rl.core.services.interfaces

import EventEmitter
import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.laboratory.session.LabSession
import isel.rl.core.utils.Either

typealias CreateLabSessionResult = Either<ServicesExceptions, LabSession>
typealias StartLabSessionResult = Either<ServicesExceptions, Unit>

interface ILabSessionService {
    fun createLabSession(
        labId: String,
        userId: Int,
    ): CreateLabSessionResult

    suspend fun startLabSession(
        listener: EventEmitter,
        labId: Int,
        labSessionId: Int,
    ): StartLabSessionResult

    /*fun startLabSession(
        labId: String,
        userId: Int
    ): StartLabSessionResult

     */
}
