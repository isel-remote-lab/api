package isel.rl.core.repository.jdbi.mappers.laboratory

import isel.rl.core.domain.config.DomainConfig
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

class LabMapper(
    private val labsDomainConfig: DomainConfig.LaboratoryRestrictions,
) : RowMapper<Laboratory> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Laboratory =
        Laboratory(
            id = rs.getInt(Laboratory.ID_PROP),
            name = LabName(rs.getString(Laboratory.LAB_NAME_PROP)),
            description = LabDescription(rs.getString(Laboratory.LAB_DESCRIPTION_PROP)),
            duration =
                LabDuration(
                    rs.getInt(Laboratory.LAB_DURATION_PROP)
                        .toDuration(DurationUnit.valueOf(labsDomainConfig.duration.unit)),
                ),
            queueLimit = LabQueueLimit(rs.getInt(Laboratory.LAB_QUEUE_LIMIT_PROP)),
            createdAt = rs.getTimestamp(Laboratory.CREATED_AT_PROP).toInstant().toKotlinInstant(),
            ownerId = rs.getInt(Laboratory.OWNER_ID_PROP),
        )
}
