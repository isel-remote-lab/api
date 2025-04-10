package rl.jdbi.mappers.laboratory

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.laboratory.LabSessionState
import java.sql.ResultSet
import java.sql.SQLException

class LabSessionStateMapper : ColumnMapper<LabSessionState> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): LabSessionState {
        val labSessionChar = r.getString(columnNumber)
        return LabSessionState.entries.firstOrNull { it.char == labSessionChar }
            ?: throw SQLException("Unknown role: $labSessionChar")
    }
}