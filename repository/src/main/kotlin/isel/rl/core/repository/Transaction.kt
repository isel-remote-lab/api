package isel.rl.core.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    val usersRepository: UsersRepository
    val laboratoriesRepository: LaboratoriesRepository
    val groupsRepository: GroupsRepository
    val hardwareRepository: HardwareRepository

    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}
