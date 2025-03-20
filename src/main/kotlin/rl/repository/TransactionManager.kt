package rl.repository

/**
 * Transaction manager.
 */
interface TransactionManager {
    /**
     * Runs a block of code within a transaction.
     *
     * @param block the block of code to be executed within the transaction
     * @return the result of the block execution
     */
    fun <R> run(block: (Transaction) -> R): R
}