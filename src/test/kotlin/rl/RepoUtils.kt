package rl

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.domain.laboratory.LabName
import rl.domain.user.Email
import rl.domain.user.Role
import rl.domain.user.Username
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class RepoUtils {
    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    fun newTestUsername() = Username("user-${abs(Random.nextLong())}")

    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    fun newTestLabName() = LabName("lab-${abs(Random.nextLong())}")

    fun newTestLabDuration() = abs(Random.nextInt()).toDuration(DurationUnit.MINUTES)

    fun randomLabQueueLimit() = (1..50).random()

    fun randomUserRole() = Role.entries.random()

    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()
}