package isel.rl.core.services

import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import isel.rl.core.repository.jdbi.transaction.JdbiTransactionManager
import isel.rl.core.security.JWTUtils
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Utility class for generating test data and services.
 */
class ServicesUtils {
    // General

    /**
     * [Jdbi] instance configured with a PostgreSQL data source.
     */
    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
            },
        ).configureWithAppRequirements()

    private val secrets = RemoteLabApp().secrets()

    // User functions

    /**
     * Creates a new instance of the [UsersService] with a [JdbiTransactionManager] and [UsersDomain].
     *
     * @param testClock The [TestClock] instance to be used.
     * @return A new instance of [UsersService].
     */
    fun createUsersServices(testClock: TestClock): UsersService =
        UsersService(
            JdbiTransactionManager(jdbi),
            UsersDomain(),
            JWTUtils(secrets),
            testClock,
        )

    /**
     * Generates a random username for testing purposes.
     */
    fun newTestUsername() = "user-${abs(Random.nextLong())}"

    /**
     * Generates a random email for testing purposes.
     */
    fun newTestEmail() = "email-${abs(Random.nextLong())}"

    /**
     * Generates a random user role for testing purposes.
     */
    fun randomUserRole() = Role.entries.random().char

    /**
     * Generates a random oauthId for testing purposes.
     */
    fun newTestOauthId() = "oauth-${abs(Random.nextLong())}"

    // Group functions

    /**
     * Generates a random group name for testing purposes.
     */
    fun newTestGroupName() = "group-${abs(Random.nextLong())}"

    /**
     * Generates a random group description for testing purposes.
     */
    fun newTestGroupDescription() = "description-${abs(Random.nextLong())}"

    // Lab functions

    /**
     * Generates a random lab name for testing purposes.
     */
    fun newTestLabName() = "lab-${abs(Random.nextLong())}"

    /**
     * Generates a random lab description for testing purposes.
     */
    fun newTestLabDescription() = "description-${abs(Random.nextLong())}"

    /**
     * Generates a random lab duration for testing purposes.
     */
    fun newTestLabDuration() = abs(Random.nextInt()).toDuration(DurationUnit.MINUTES).toString()

    /**
     * Generates a random lab queue limit for testing purposes.
     */
    fun randomLabQueueLimit() = (1..50).random().toString()

    /**
     * Generates a random lab session state for testing purposes.
     */
    fun randomLabSessionState() = LabSessionState.entries.random().char

    // Hardware functions

    /**
     * Generates a random hardware name for testing purposes.
     */
    fun newTestHardwareName() = "hardware-${abs(Random.nextLong())}"

    /**
     * Generates a random hardware serial number for testing purposes.
     */
    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"

    /**
     * Generates a random hardware status for testing purposes.
     */
    fun randomHardwareStatus() = HardwareStatus.entries.random().char

    /**
     * Generates a random hardware mac address for testing purposes.
     */
    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"

    /**
     * Generates a random hardware IP address for testing purposes.
     */
    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

    /**
     * Generates a random token validation data for testing purposes.
     */
    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}
