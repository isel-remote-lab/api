package isel.rl.core.repository.utils

import isel.rl.core.domain.group.GroupDescription
import isel.rl.core.domain.group.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.repository.jdbi.*
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class RepoUtils {
    val usersDomain = UsersDomain()
    val laboratoriesDomain = LaboratoriesDomain(
        RemoteLabApp().laboratoryDomainConfig()
    )


    // General
    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()

    // User functions
    fun newTestUsername() = Username("user-${abs(Random.nextLong())}")
    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")
    fun randomUserRole() = Role.entries.random()
    fun newTestOauthId() = OAuthId("oauth-${abs(Random.nextLong())}")

    fun createTestUser(handle: Handle): Int {
        val userRepo = JdbiUsersRepository(handle)
        val userDomain = UsersDomain()
        val clock = TestClock()

        // when: storing a user
        val username = newTestUsername()
        val email = newTestEmail()
        val createdAt = clock.now()
        val userRole = randomUserRole()
        val oAuthId = newTestOauthId()

        return userRepo.createUser(
            userDomain.validateCreateUser(
                oAuthId.oAuthIdInfo, userRole.char, username.usernameInfo, email.emailInfo, createdAt
            )
        )
    }

    // Group functions
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")
    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    fun createTestGroup(userId: Int, handle: Handle): Int {
        val groupRepo = JdbiGroupRepository(handle)
        val clock = TestClock()

        // when: storing a group
        val groupName = newTestGroupName()
        val groupDescription = newTestGroupDescription()
        val groupCreatedAt = clock.now()

        return groupRepo.createGroup(groupName, groupDescription, groupCreatedAt, userId)
    }

    // Lab functions
    fun newTestLabName() = LabName("lab-${abs(Random.nextLong())}")
    fun newTestLabDescription() = LabDescription("description-${abs(Random.nextLong())}")
    fun newTestLabDuration() = (1..100).random().toDuration(DurationUnit.MINUTES)
    fun randomLabQueueLimit() = (1..50).random()
    fun randomLabSessionState() = LabSessionState.entries.random()

    fun createTestLab(handle: Handle): Int {
        val laboratoryRepo = JdbiLaboratoriesRepository(handle)
        val labDomain = LaboratoriesDomain(
            RemoteLabApp().laboratoryDomainConfig()
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
                labDuration.toInt(DurationUnit.MINUTES),
                randomLabQueueLimit,
                labCreatedAt,
                userId
            )
        )
    }

    // Hardware functions
    fun newTestHardwareName() = HardwareName("hardware-${abs(Random.nextLong())}")
    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"
    fun randomHardwareStatus() = HardwareStatus.entries.random()
    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"
    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

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
            createdAt = createdAt
        )
    }

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}