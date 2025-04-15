package isel.rl.core.repository.jdbi.mappers.laboratory

import isel.rl.core.domain.laboratory.LabName
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class LabNameMapper : ColumnMapper<LabName> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): LabName = LabName(r.getString(columnNumber))
}
