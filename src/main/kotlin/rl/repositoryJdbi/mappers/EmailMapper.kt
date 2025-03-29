package rl.repositoryJdbi.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.user.Email
import java.sql.ResultSet
import java.sql.SQLException

class EmailMapper : ColumnMapper<Email> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): Email = Email(r.getString(columnNumber))
}