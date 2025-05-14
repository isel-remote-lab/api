package isel.rl.core.domain.config

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class LaboratoriesDomainConfig(
    val minLabNameLength: Int,
    val maxLabNameLength: Int,
    val isLabNameOptional: Boolean,
    val minLabDescriptionLength: Int,
    val maxLabDescriptionLength: Int,
    val isLabDescriptionOptional: Boolean,
    val minLabDuration: Duration,
    val maxLabDuration: Duration,
    val isLabDurationOptional: Boolean,
    val labDurationUnit: DurationUnit,
    val minLabQueueLimit: Int,
    val maxLabQueueLimit: Int,
    val isLabQueueLimitOptional: Boolean,
) {
    companion object {
        fun from(config: DomainConfig.LaboratoryRestrictions): LaboratoriesDomainConfig {
            val labDurationUnit = DurationUnit.valueOf(config.labDuration.unit)

            return LaboratoriesDomainConfig(
                minLabNameLength = config.labName.min,
                maxLabNameLength = config.labName.max,
                isLabNameOptional = config.labName.optional,
                minLabDescriptionLength = config.labDescription.min,
                maxLabDescriptionLength = config.labDescription.max,
                isLabDescriptionOptional = config.labDescription.optional,
                minLabDuration = config.labDuration.min.toDuration(labDurationUnit),
                maxLabDuration = config.labDuration.max.toDuration(labDurationUnit),
                isLabDurationOptional = config.labDuration.optional,
                labDurationUnit = labDurationUnit,
                minLabQueueLimit = config.labQueueLimit.min,
                maxLabQueueLimit = config.labQueueLimit.max,
                isLabQueueLimitOptional = config.labQueueLimit.optional,
            )
        }
    }
}
