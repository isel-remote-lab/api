package rl.repositoryJdbi.mappers.user

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.user.Username
import java.sql.ResultSet
import java.sql.SQLException

class UsernameMapper : ColumnMapper<Username> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): Username = Username(r.getString(columnNumber))
}