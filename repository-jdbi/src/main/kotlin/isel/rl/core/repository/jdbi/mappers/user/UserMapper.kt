package isel.rl.core.repository.jdbi.mappers.user

import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
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
        val roleChar = rs.getString(User.ROLE_PROP)

        return User(
            id = rs.getInt(User.ID_PROP),
            role =
                Role.entries.firstOrNull { it.char == roleChar }
                    ?: throw SQLException("Unknown role: $roleChar"),
            name = Name(rs.getString(User.NAME_PROP)),
            email = Email(rs.getString(User.EMAIL_PROP)),
            createdAt = rs.getTimestamp(User.CREATED_AT_PROP).toInstant().toKotlinInstant(),
        )
    }
}
