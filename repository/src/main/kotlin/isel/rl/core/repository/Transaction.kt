package isel.rl.core.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    val usersRepository: UsersRepository
    val laboratoriesRepository: LaboratoriesRepository

    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}
