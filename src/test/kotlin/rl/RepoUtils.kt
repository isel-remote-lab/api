package rl

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import rl.domain.user.Email
import kotlin.math.abs
import kotlin.random.Random

class RepoUtils {
    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    fun newTestGroupName() = "group-${abs(Random.nextLong())}"

    fun newTestGroupDescription() = "description-${abs(Random.nextLong())}"

    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()
}