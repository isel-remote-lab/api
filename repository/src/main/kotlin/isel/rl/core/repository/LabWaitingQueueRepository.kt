package isel.rl.core.repository

interface LabWaitingQueueRepository {
    fun addUserToLabQueue(
        labId: Int,
        userId: Int,
    ): Boolean

    fun removeUserLabQueue(
        labId: Int,
        userId: Int,
    ): Boolean

    fun isLabQueueEmpty(labId: Int): Boolean

    fun getUserQueuePosition(
        labId: Int,
        userId: Int,
    ): Int

    fun getQueueSize(labId: Int): Int

    /**
     * Returns user ID
     */
    fun popLabQueue(labId: Int): Int

    fun getUsersInQueue(labId: Int): List<Int>
}
