package isel.rl.core.domain.config

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Represents the domain configuration for laboratories.
 */
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
        /**
         * Creates a [LaboratoriesDomainConfig] instance from the provided [DomainConfig.LaboratoryRestrictions].
         *
         * @param config The laboratory restrictions configuration.
         * @return A new instance of [LaboratoriesDomainConfig].
         */
        fun from(config: DomainConfig.LaboratoryRestrictions): LaboratoriesDomainConfig {
            val labDurationUnit = DurationUnit.valueOf(config.duration.unit)

            return LaboratoriesDomainConfig(
                minLabNameLength = config.name.min,
                maxLabNameLength = config.name.max,
                isLabNameOptional = config.name.optional,
                minLabDescriptionLength = config.description.min,
                maxLabDescriptionLength = config.description.max,
                isLabDescriptionOptional = config.description.optional,
                minLabDuration = config.duration.min.toDuration(labDurationUnit),
                maxLabDuration = config.duration.max.toDuration(labDurationUnit),
                isLabDurationOptional = config.duration.optional,
                labDurationUnit = labDurationUnit,
                minLabQueueLimit = config.queueLimit.min,
                maxLabQueueLimit = config.queueLimit.max,
                isLabQueueLimitOptional = config.queueLimit.optional,
            )
        }
    }
}
