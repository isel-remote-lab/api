package isel.rl.core.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    val usersRepository: UsersRepository
    val laboratoriesRepository: LaboratoriesRepository
    val groupRepository: GroupsRepository

    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}
