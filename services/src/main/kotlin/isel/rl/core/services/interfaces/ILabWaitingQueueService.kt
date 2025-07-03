package isel.rl.core.services.interfaces

import EventEmitter

interface ILabWaitingQueueService {
    suspend fun pushUserIntoQueue(
        labId: Int,
        userId: Int,
        listener: EventEmitter,
    )

    fun popUserFromQueue(labId: Int)

    fun updateQueuePositions(labId: Int)
}
