package isel.rl.core.repository.jdbi.mappers.laboratory

import isel.rl.core.domain.laboratory.LabDescription
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class LabDescriptionMapper : ColumnMapper<LabDescription> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): LabDescription = LabDescription(r.getString(columnNumber))
}
