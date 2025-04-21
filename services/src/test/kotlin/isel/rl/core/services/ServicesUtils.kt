package isel.rl.core.services

import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Role
import isel.rl.core.repository.jdbi.*
import isel.rl.core.repository.jdbi.transaction.JdbiTransactionManager
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ServicesUtils {
    // General
    private val jdbi = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
        },
    ).configureWithAppRequirements()

    // User functions
    fun createUsersServices(
        testClock: TestClock,
    ): UsersService =
        UsersService(
            JdbiTransactionManager(jdbi),
            UsersDomain(),
            testClock,
        )

    fun newTestUsername() = "user-${abs(Random.nextLong())}"
    fun newTestEmail() = "email-${abs(Random.nextLong())}"
    fun randomUserRole() = Role.entries.random().char
    fun newTestOauthId() = "oauth-${abs(Random.nextLong())}"

    // Group functions
    fun newTestGroupName() = "group-${abs(Random.nextLong())}"
    fun newTestGroupDescription() = "description-${abs(Random.nextLong())}"

    // Lab functions
    fun newTestLabName() = "lab-${abs(Random.nextLong())}"
    fun newTestLabDescription() = "description-${abs(Random.nextLong())}"
    fun newTestLabDuration() = abs(Random.nextInt()).toDuration(DurationUnit.MINUTES).toString()
    fun randomLabQueueLimit() = (1..50).random().toString()
    fun randomLabSessionState() = LabSessionState.entries.random().char

    // Hardware functions
    fun newTestHardwareName() = "hardware-${abs(Random.nextLong())}"
    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"
    fun randomHardwareStatus() = HardwareStatus.entries.random().char
    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"
    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}