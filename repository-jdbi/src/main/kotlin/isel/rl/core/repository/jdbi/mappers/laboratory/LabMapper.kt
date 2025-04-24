package isel.rl.core.repository.jdbi.mappers.laboratory

import isel.rl.core.domain.laboratory.Laboratory
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabDuration
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.props.LabQueueLimit
import kotlinx.datetime.toKotlinInstant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class LabMapper : RowMapper<Laboratory> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Laboratory =
        Laboratory(
            id = rs.getInt("id"),
            labName = LabName(rs.getString("lab_name")),
            labDescription = LabDescription(rs.getString("lab_description")),
            labDuration = LabDuration(rs.getInt("lab_duration").toDuration(DurationUnit.MINUTES)),
            labQueueLimit = LabQueueLimit(rs.getInt("lab_queue_limit")),
            createdAt = rs.getTimestamp("created_at").toInstant().toKotlinInstant(),
            ownerId = rs.getInt("owner_id"),
        )
}
