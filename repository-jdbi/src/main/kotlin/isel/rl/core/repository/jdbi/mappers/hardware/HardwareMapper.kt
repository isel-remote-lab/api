package isel.rl.core.repository.jdbi.mappers.hardware

import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.*
import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class HardwareMapper : RowMapper<Hardware> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Hardware {
        val hardwareStatus = rs.getString(Hardware.STATUS_PROP)
        val macAddress = rs.getString(Hardware.MAC_ADDRESS_PROP)
        val ipAddress = rs.getString(Hardware.IP_ADDRESS_PROP)

        return Hardware(
            id = rs.getInt(Hardware.ID_PROP),
            name = HardwareName(rs.getString(Hardware.NAME_PROP)),
            serialNumber = SerialNumber(rs.getString(Hardware.SERIAL_NUMBER_PROP)),
            status = HardwareStatus.entries.firstOrNull { it.char == hardwareStatus }
                ?: throw SQLException("Unknown role: $hardwareStatus"),
            macAddress = if (macAddress.isNullOrBlank()) null else MacAddress(macAddress),
            ipAddress = if (ipAddress.isNullOrBlank()) null else IpAddress(ipAddress),
            createdAt = rs.getTimestamp(Hardware.CREATED_AT_PROP).toInstant().toKotlinInstant()
        )
    }
}