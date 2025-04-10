package rl.jdbi.mappers.hardware

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.hardware.HardwareName
import java.sql.ResultSet
import java.sql.SQLException

class HardwareNameMapper : ColumnMapper<HardwareName> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): HardwareName = HardwareName(r.getString(columnNumber))
}