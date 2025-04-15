package isel.rl.core.repository.jdbi.mappers.group

import isel.rl.core.domain.group.GroupDescription
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class GroupDescriptionMapper : ColumnMapper<GroupDescription> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): GroupDescription = GroupDescription(r.getString(columnNumber))
}
