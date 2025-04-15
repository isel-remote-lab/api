package isel.rl.core.repository.jdbi.mappers.user

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.UserFactory
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class UserMapper : RowMapper<User> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): User {
        val roleChar = rs.getString("role")

        return UserFactory.createUser(
            id = rs.getInt("id"),
            oauthId = OAuthId(rs.getString("o_auth_id")),
            role =
                Role.entries.firstOrNull { it.char == roleChar }
                    ?: throw SQLException("Unknown role: $roleChar"),
            username = Username(rs.getString("username")),
            email = Email(rs.getString("email")),
            createdAt = rs.getTimestamp("created_at").toInstant().toKotlinInstant(),
        )
    }
}
