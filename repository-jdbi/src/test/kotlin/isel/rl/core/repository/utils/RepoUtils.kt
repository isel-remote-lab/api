package isel.rl.core.repository.utils

import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.repository.jdbi.JdbiGroupsRepository
import isel.rl.core.repository.jdbi.JdbiHardwareRepository
import isel.rl.core.repository.jdbi.JdbiLaboratoriesRepository
import isel.rl.core.repository.jdbi.JdbiUsersRepository
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Utility class for creating test data and managing database connections.
 * This class provides methods to create test users, groups, laboratories, and hardware,
 */
class RepoUtils {
    private val userDomainConfig = RemoteLabApp().usersDomainConfig()
    private val labDomainConfig = RemoteLabApp().laboratoriesDomainConfig()
    private val groupDomainConfig = RemoteLabApp().groupsDomainConfig()
    val secrets = RemoteLabApp().secrets()
    private val tokenEncoder = RemoteLabApp().tokenEncoder()

    /**
     * Provides a [UsersDomain] instance for validating user-related operations.
     */
    val usersDomain =
        UsersDomain(
            userDomainConfig,
            tokenEncoder,
        )

    /**
     * Provides a [LaboratoriesDomain] instance for validating laboratory-related operations.
     */
    val laboratoriesDomain =
        LaboratoriesDomain(
            labDomainConfig,
        )

    /**
     * Provides a [GroupsDomain] instance for validating user-related operations.
     */
    val groupsDomain =
        GroupsDomain(
            groupDomainConfig,
        )


    // General

    /**
     * Creates a new database connection handle.
     * @return A new [Handle] instance for database operations.
     */
    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    /**
     * [Jdbi] instance configured with a PostgreSQL data source.
     */
    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()

    // User functions

    /**
     * Generates a random username for testing purposes.
     */
    fun newTestUsername() = Username("user-${abs(Random.nextLong())}")

    /**
     * Generates a random email for testing purposes.
     */
    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")

    /**
     * Generates a random user role for testing purposes.
     */
    fun randomUserRole() = Role.entries.random()

    /**
     * Generates a random oauthId for testing purposes.
     */
    fun newTestOauthId() = OAuthId("oauth-${abs(Random.nextLong())}")

    /**
     * Creates a new user in the database and returns its ID.
     * @param handle The [Handle] instance for database operations.
     * @return The ID of the created user.
     */
    fun createTestUser(handle: Handle): Int {
        val userRepo = JdbiUsersRepository(handle)
        val clock = TestClock()

        // when: storing a user
        val username = newTestUsername()
        val email = newTestEmail()
        val createdAt = clock.now()
        val userRole = randomUserRole()
        val oAuthId = newTestOauthId()

        return userRepo.createUser(
            usersDomain.validateCreateUser(
                oAuthId.oAuthIdInfo,
                userRole.char,
                username.usernameInfo,
                email.emailInfo,
                createdAt,
            ),
        )
    }

    // Group functions

    /**
     * Generates a random group name for testing purposes.
     */
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    /**
     * Generates a random group description for testing purposes.
     */
    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    /**
     * Creates a new group in the database and returns its ID.
     * @param userId The ID of the user creating the group.
     * @param handle The [Handle] instance for database operations.
     * @return The ID of the created group.
     */
    fun createTestGroup(
        userId: Int,
        handle: Handle,
    ): Int {
        val groupRepo = JdbiGroupsRepository(handle)
        val clock = TestClock()

        // when: storing a group
        val groupName = newTestGroupName()
        val groupDescription = newTestGroupDescription()
        val groupCreatedAt = clock.now()

        return groupRepo.createGroup(
            groupsDomain.validateCreateGroup(
                groupName.groupNameInfo,
                groupDescription.groupDescriptionInfo,
                groupCreatedAt,
                userId
            )
        )

    }

    // Lab functions

    /**
     * Generates a random lab name for testing purposes.
     */
    fun newTestLabName() = LabName("lab-${abs(Random.nextLong())}")

    /**
     * Generates a random lab description for testing purposes.
     */
    fun newTestLabDescription() = LabDescription("description-${abs(Random.nextLong())}")

    /**
     * Generates a random lab duration for testing purposes.
     */
    fun newTestLabDuration() = LabDuration((1..100).random().toDuration(DurationUnit.MINUTES))

    /**
     * Generates a random lab queue limit for testing purposes.
     */
    fun randomLabQueueLimit() = LabQueueLimit((1..50).random())

    /**
     * Generates a random lab session state for testing purposes.
     */
    fun randomLabSessionState() = LabSessionState.entries.random()

    /**
     * Creates a new laboratory in the database and returns its ID.
     * @param handle The [Handle] instance for database operations.
     * @return The ID of the created laboratory.
     */
    fun createTestLab(handle: Handle): Int {
        val laboratoryRepo = JdbiLaboratoriesRepository(handle)
        val labDomain =
            LaboratoriesDomain(
                RemoteLabApp().laboratoriesDomainConfig(),
            )
        val clock = TestClock()

        // when: storing a laboratory
        val labName = newTestLabName()
        val labDescription = newTestLabDescription()
        val labDuration = newTestLabDuration()
        val labCreatedAt = clock.now()
        val randomLabQueueLimit = randomLabQueueLimit()
        val userId = createTestUser(handle)

        return laboratoryRepo.createLaboratory(
            labDomain.validateCreateLaboratory(
                labName.labNameInfo,
                labDescription.labDescriptionInfo,
                labDuration.labDurationInfo.toInt(DurationUnit.MINUTES),
                randomLabQueueLimit.labQueueLimitInfo,
                labCreatedAt,
                userId,
            ),
        )
    }

    // Hardware functions

    /**
     * Generates a random hardware name for testing purposes.
     */
    fun newTestHardwareName() = HardwareName("hardware-${abs(Random.nextLong())}")

    /**
     * Generates a random hardware serial number for testing purposes.
     */
    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"

    /**
     * Generates a random hardware status for testing purposes.
     */
    fun randomHardwareStatus() = HardwareStatus.entries.random()

    /**
     * Generates a random hardware mac address for testing purposes.
     */
    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"

    /**
     * Generates a random hardware IP address for testing purposes.
     */
    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

    /**
     * Creates a new hardware in the database and returns its ID.
     * @param handle The [Handle] instance for database operations.
     * @return The ID of the created hardware.
     */
    fun createTestHardware(handle: Handle): Int {
        val hardwareRepo = JdbiHardwareRepository(handle)
        val clock = TestClock()

        val hardwareName = newTestHardwareName()
        val serialNum = newTestHardwareSerialNumber()
        val status = randomHardwareStatus()
        val macAddress = newTestHardwareMacAddress()
        val ipAddress = newTestHardwareIpAddress()
        val createdAt = clock.now()

        return hardwareRepo.createHardware(
            name = hardwareName,
            serialNum = serialNum,
            status = status,
            macAddress = macAddress,
            ipAddress = ipAddress,
            createdAt = createdAt,
        )
    }

    /**
     * Generates a random token validation data for testing purposes.
     */
    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}
