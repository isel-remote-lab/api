package rl.jdbi.transaction

import jakarta.inject.Named
import org.jdbi.v3.core.Jdbi
import rl.repository.Transaction
import rl.repository.TransactionManager

@Named
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}