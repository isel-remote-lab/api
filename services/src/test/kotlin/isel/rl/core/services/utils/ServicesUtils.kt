package isel.rl.core.services.utils

import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.laboratory.domain.LaboratoriesDomain
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.host.RemoteLabApp
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random

/**
 * Utility class for generating test data and services.
 */
object ServicesUtils {
    // General
    private val remoteLab = RemoteLabApp()
    val domainConfigs = remoteLab.domainConfigs
    private val tokenEncoder = remoteLab.tokenEncoder()
    private val usersDomainConfig = remoteLab.usersDomainConfig()
    val labsDomainConfig = remoteLab.laboratoriesDomainConfig()
    private val groupsDomainConfig = remoteLab.groupsDomainConfig()

    /**
     * [Jdbi] instance configured with a PostgreSQL data source.
     */
    val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
            },
        ).configureWithAppRequirements(
            domainConfigs,
        )

    val usersDomain =
        UsersDomain(
            usersDomainConfig,
            tokenEncoder,
        )

    val groupsDomain = GroupsDomain(groupsDomainConfig)

    val laboratoriesDomain = LaboratoriesDomain(labsDomainConfig)

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
