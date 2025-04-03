package rl

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.domain.hardware.HardwareName
import rl.domain.hardware.HardwareStatus
import rl.domain.laboratory.LabName
import rl.domain.user.Email
import rl.domain.user.Role
import rl.domain.user.Username
import rl.domain.user.token.TokenValidationInfo
import rl.repositoryJdbi.mappers.user.EmailMapper
import rl.repositoryJdbi.mappers.InstantMapper
import rl.repositoryJdbi.mappers.TokenValidationInfoMapper
import rl.repositoryJdbi.mappers.group.GroupDescriptionMapper
import rl.repositoryJdbi.mappers.group.GroupNameMapper
import rl.repositoryJdbi.mappers.hardware.HardwareNameMapper
import rl.repositoryJdbi.mappers.hardware.HardwareStatusMapper
import rl.repositoryJdbi.mappers.laboratory.LabNameMapper
import rl.repositoryJdbi.mappers.user.RoleMapper
import rl.repositoryJdbi.mappers.user.UsernameMapper

@SpringBootApplication
class RemoteLabApp {
    /**
     * Creates and configures a Jdbi instance.
     *
     * @return the configured Jdbi instance
     */
    @Bean
    fun jdbi() =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()
}

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    // User Mappers
    registerColumnMapper(Username::class.java, UsernameMapper())
    registerColumnMapper(Email::class.java, EmailMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerColumnMapper(Role::class.java, RoleMapper())

    // Group Mappers
    registerColumnMapper(GroupName::class.java, GroupNameMapper())
    registerColumnMapper(GroupDescription::class.java, GroupDescriptionMapper())

    // Laboratory Mappers
    registerColumnMapper(LabName::class.java, LabNameMapper())

    // Hardware Mappers
    registerColumnMapper(HardwareName::class.java, HardwareNameMapper())
    registerColumnMapper(HardwareStatus::class.java, HardwareStatusMapper())

    // General Mappers
    registerColumnMapper(Instant::class.java, InstantMapper())

    return this
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}