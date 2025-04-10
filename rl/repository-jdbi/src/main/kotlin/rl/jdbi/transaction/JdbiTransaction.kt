package rl.jdbi.transaction

import org.jdbi.v3.core.Handle
import rl.jdbi.JdbiUsersRepository
import rl.repository.Transaction
import rl.repository.UsersRepository

/**
 * JdbiTransaction class that implements the Transaction interface.
 * This class manages various repositories and provides a rollback mechanism.
 *
 * @property handle the Jdbi Handle used for database interactions
 */
class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
