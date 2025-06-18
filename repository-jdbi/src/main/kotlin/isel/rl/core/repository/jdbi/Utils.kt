package isel.rl.core.repository.jdbi

import isel.rl.core.domain.config.DomainConfig
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.HardwareName
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.token.TokenValidationInfo
import isel.rl.core.repository.jdbi.mappers.InstantMapper
import isel.rl.core.repository.jdbi.mappers.TokenValidationInfoMapper
import isel.rl.core.repository.jdbi.mappers.group.GroupMapper
import isel.rl.core.repository.jdbi.mappers.hardware.HardwareMapper
import isel.rl.core.repository.jdbi.mappers.laboratory.LabMapper
import isel.rl.core.repository.jdbi.mappers.laboratory.LabSessionStateMapper
import isel.rl.core.repository.jdbi.mappers.user.UserMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(domainConfig: DomainConfig): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    // User Mappers
    registerRowMapper(User::class.java, UserMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())

    // Group Mappers
    registerRowMapper(Group::class.java, GroupMapper())

    // Laboratory Mappers
    registerRowMapper(Laboratory::class.java, LabMapper(domainConfig.laboratory))
    registerColumnMapper(LabSessionState::class.java, LabSessionStateMapper())

    // Hardware Mappers
    registerRowMapper(Hardware::class.java, HardwareMapper())

    // General Mappers
    registerColumnMapper(Instant::class.java, InstantMapper())

    return this
}
