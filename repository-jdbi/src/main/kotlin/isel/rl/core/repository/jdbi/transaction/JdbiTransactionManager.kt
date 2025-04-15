package isel.rl.core.repository.jdbi.transaction

import isel.rl.core.repository.Transaction
import isel.rl.core.repository.TransactionManager
import jakarta.inject.Named
import org.jdbi.v3.core.Jdbi

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
