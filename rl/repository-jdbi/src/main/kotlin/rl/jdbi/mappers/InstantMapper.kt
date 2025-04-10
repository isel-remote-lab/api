package rl.jdbi.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class InstantMapper : ColumnMapper<Instant> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Instant? {
        val columnValue = rs.getTimestamp(columnNumber)
        return columnValue?.toInstant()?.toKotlinInstant()
    }
}