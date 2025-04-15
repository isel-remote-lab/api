package isel.rl.core.repository.jdbi.mappers.hardware

import isel.rl.core.domain.hardware.HardwareStatus
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class HardwareStatusMapper : ColumnMapper<HardwareStatus> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): HardwareStatus {
        val roleChar = r.getString(columnNumber)
        return HardwareStatus.entries.firstOrNull { it.char == roleChar }
            ?: throw SQLException("Unknown hardware status: $roleChar")
    }
}
