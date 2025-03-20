package rl.repository

/**
 * Represents a transaction that involves various repositories.
 */
interface Transaction {
    /**
     * Rolls back the current transaction.
     */
    fun rollback()
}