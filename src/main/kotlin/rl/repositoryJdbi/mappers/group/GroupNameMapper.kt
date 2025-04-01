package rl.repositoryJdbi.mappers.group

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.group.GroupName
import java.sql.ResultSet
import java.sql.SQLException

class GroupNameMapper : ColumnMapper<GroupName> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): GroupName = GroupName(r.getString(columnNumber))
}