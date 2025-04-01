package rl.repositoryJdbi.mappers.laboratory

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.laboratory.LabName
import java.sql.ResultSet
import java.sql.SQLException

class LabNameMapper: ColumnMapper<LabName> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): LabName = LabName(r.getString(columnNumber))
}