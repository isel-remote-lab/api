package isel.rl.core.repository.jdbi.mappers.group

import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
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
            id = rs.getInt("id"),
            groupName = GroupName(rs.getString("group_name")),
            groupDescription = GroupDescription(rs.getString("group_description")),
            createdAt = rs.getTimestamp("created_at").toInstant().toKotlinInstant(),
            ownerId = rs.getInt("owner_id"),
        )
}
