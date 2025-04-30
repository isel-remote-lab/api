package isel.rl.core.repository.jdbi.transaction

import isel.rl.core.repository.GroupRepository
import isel.rl.core.repository.LaboratoriesRepository
import isel.rl.core.repository.Transaction
import isel.rl.core.repository.UsersRepository
import isel.rl.core.repository.jdbi.JdbiGroupRepository
import isel.rl.core.repository.jdbi.JdbiLaboratoriesRepository
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import org.jdbi.v3.core.Handle

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
    override val laboratoriesRepository: LaboratoriesRepository = JdbiLaboratoriesRepository(handle)
    override val groupRepository: GroupRepository = JdbiGroupRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
