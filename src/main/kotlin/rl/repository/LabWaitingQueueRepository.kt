package rl.repository

interface LabWaitingQueueRepository {
    fun addUserToLabQueue(
        labId: Int,
        userId: Int
    ): Boolean

    fun removeUserLabQueue(
        labId: Int,
        userId: Int
    ): Boolean

    fun isLabQueueEmpty(
        labId: Int
    ): Boolean

    /**
     * Returns user ID
     */
    fun popLabQueue(
        labId: Int
    ): Int
}