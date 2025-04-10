package rl.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import rl.domain.group.GroupDescription
import rl.domain.group.GroupName
import rl.domain.hardware.HardwareName
import rl.domain.hardware.HardwareStatus
import rl.domain.laboratory.LabDescription
import rl.domain.laboratory.LabName
import rl.domain.laboratory.LabSessionState
import rl.domain.user.User
import rl.domain.user.token.TokenValidationInfo
import rl.jdbi.mappers.InstantMapper
import rl.jdbi.mappers.TokenValidationInfoMapper
import rl.jdbi.mappers.group.GroupDescriptionMapper
import rl.jdbi.mappers.group.GroupNameMapper
import rl.jdbi.mappers.hardware.HardwareNameMapper
import rl.jdbi.mappers.hardware.HardwareStatusMapper
import rl.jdbi.mappers.laboratory.LabDescriptionMapper
import rl.jdbi.mappers.laboratory.LabNameMapper
import rl.jdbi.mappers.laboratory.LabSessionStateMapper
import rl.jdbi.mappers.UserMapper

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    // User Mappers
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerRowMapper(User::class.java, UserMapper())

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