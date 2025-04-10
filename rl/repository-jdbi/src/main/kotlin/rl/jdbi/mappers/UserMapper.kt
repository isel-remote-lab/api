package rl.jdbi.mappers

import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import rl.domain.user.*
import rl.domain.user.User
import rl.domain.user.UserFactory
import java.sql.ResultSet
import java.sql.SQLException

class UserMapper : RowMapper<User> {
    override fun map(rs: ResultSet, ctx: StatementContext): User {
        val roleChar = rs.getString("role")

        return UserFactory.createUser(
            id = rs.getInt("id"),
            oauthId = OAuthId(rs.getString("o_auth_id")),
            role = Role.entries.firstOrNull { it.char == roleChar }
                ?: throw SQLException("Unknown role: $roleChar"),
            username = Username(rs.getString("username")),
            email = Email(rs.getString("email")),
            createdAt = rs.getTimestamp("created_at").toInstant().toKotlinInstant()
        )
    }
}