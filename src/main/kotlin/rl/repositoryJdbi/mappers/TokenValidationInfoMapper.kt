package rl.repositoryJdbi.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.user.token.TokenValidationInfo
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Maps SQL column values to the TokenValidationInfo type.
 */
class TokenValidationInfoMapper : ColumnMapper<TokenValidationInfo> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): TokenValidationInfo = TokenValidationInfo(r.getString(columnNumber))
}