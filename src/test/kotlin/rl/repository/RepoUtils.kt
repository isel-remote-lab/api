package rl.repository

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import rl.Environment
import rl.TestClock
import rl.configureWithAppRequirements
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.domain.hardware.HardwareName
import rl.domain.hardware.HardwareStatus
import rl.domain.laboratory.LabName
import rl.domain.user.Email
import rl.domain.user.Role
import rl.domain.user.Username
import rl.repositoryJdbi.JdbiGroupRepository
import rl.repositoryJdbi.JdbiHardwareRepository
import rl.repositoryJdbi.JdbiLaboratoryRepository
import rl.repositoryJdbi.JdbiUserRepository
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class RepoUtils {
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
    fun newTestOauthId() = "oauth-${abs(Random.nextLong())}"

    fun createTestUser(handle: Handle): Int {
        val userRepo = JdbiUserRepository(handle)
        val clock = TestClock()

        // when: storing a user
        val username = newTestUsername()
        val email = newTestEmail()
        val createdAt = clock.now()
        val userRole = randomUserRole()
        val oAuthId = newTestOauthId()

        return userRepo.createUser(oAuthId, userRole, username, email, createdAt)
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
    fun newTestLabDuration() = abs(Random.nextInt()).toDuration(DurationUnit.MINUTES)
    fun randomLabQueueLimit() = (1..50).random()

    fun createTestLab(handle: Handle): Int {
        val laboratoryRepo = JdbiLaboratoryRepository(handle)
        val clock = TestClock()

        // when: storing a laboratory
        val labName = newTestLabName()
        val labDuration = newTestLabDuration()
        val labCreatedAt = clock.now()
        val randomLabQueueLimit = randomLabQueueLimit()
        val userId = createTestUser(handle)

        return laboratoryRepo.createLaboratory(labName, labDuration, randomLabQueueLimit, labCreatedAt, userId)
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