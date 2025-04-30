package isel.rl.core.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    val usersRepository: UsersRepository
    val laboratoriesRepository: LaboratoriesRepository
    val groupRepository: GroupRepository

    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}
