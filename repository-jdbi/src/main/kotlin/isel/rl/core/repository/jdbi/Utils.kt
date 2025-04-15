package isel.rl.core.repository.jdbi

import isel.rl.core.domain.group.GroupDescription
import isel.rl.core.domain.group.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.LabDescription
import isel.rl.core.domain.laboratory.LabName
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.token.TokenValidationInfo
import isel.rl.core.repository.jdbi.mappers.InstantMapper
import isel.rl.core.repository.jdbi.mappers.TokenValidationInfoMapper
import isel.rl.core.repository.jdbi.mappers.group.GroupDescriptionMapper
import isel.rl.core.repository.jdbi.mappers.group.GroupNameMapper
import isel.rl.core.repository.jdbi.mappers.hardware.HardwareNameMapper
import isel.rl.core.repository.jdbi.mappers.hardware.HardwareStatusMapper
import isel.rl.core.repository.jdbi.mappers.laboratory.LabDescriptionMapper
import isel.rl.core.repository.jdbi.mappers.laboratory.LabNameMapper
import isel.rl.core.repository.jdbi.mappers.laboratory.LabSessionStateMapper
import isel.rl.core.repository.jdbi.mappers.user.UserMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    // User Mappers
    registerRowMapper(User::class.java, UserMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())

    // Group Mappers
    registerColumnMapper(GroupName::class.java, GroupNameMapper())
    registerColumnMapper(GroupDescription::class.java, GroupDescriptionMapper())

    // Laboratory Mappers
    registerColumnMapper(LabName::class.java, LabNameMapper())
    registerColumnMapper(LabDescription::class.java, LabDescriptionMapper())
    registerColumnMapper(LabSessionState::class.java, LabSessionStateMapper())

    // Hardware Mappers
    registerColumnMapper(HardwareName::class.java, HardwareNameMapper())
    registerColumnMapper(HardwareStatus::class.java, HardwareStatusMapper())

    // General Mappers
    registerColumnMapper(Instant::class.java, InstantMapper())

    return this
}
