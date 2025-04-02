package rl.repositoryJdbi.mappers.user

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.user.Role
import java.sql.ResultSet
import java.sql.SQLException

class RoleMapper : ColumnMapper<Role> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): Role {
        val roleChar = r.getString(columnNumber)
        return Role.entries.firstOrNull { it.char == roleChar }
            ?: throw SQLException("Unknown role: $roleChar")
    }
}