package rl.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    val usersRepository: UsersRepository

    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}