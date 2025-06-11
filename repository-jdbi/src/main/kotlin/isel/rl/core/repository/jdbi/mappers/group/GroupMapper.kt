package isel.rl.core.repository.jdbi.mappers.group

import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.Group.Companion.CREATED_AT_PROP
import isel.rl.core.domain.group.Group.Companion.GROUP_DESCRIPTION_PROP
import isel.rl.core.domain.group.Group.Companion.GROUP_NAME_PROP
import isel.rl.core.domain.group.Group.Companion.OWNER_ID_PROP
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.user.User.Companion.ID_PROP
import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class GroupMapper : RowMapper<Group> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Group =
        Group(
            id = rs.getInt(ID_PROP),
            name = GroupName(rs.getString(GROUP_NAME_PROP)),
            description = GroupDescription(rs.getString(GROUP_DESCRIPTION_PROP)),
            createdAt = rs.getTimestamp(CREATED_AT_PROP).toInstant().toKotlinInstant(),
            ownerId = rs.getInt(OWNER_ID_PROP),
        )
}
