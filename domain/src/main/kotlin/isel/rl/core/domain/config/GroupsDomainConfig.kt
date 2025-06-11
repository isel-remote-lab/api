package isel.rl.core.domain.config

/**
 * Configuration for group-related domain restrictions.
 */
data class GroupsDomainConfig(
    val minLengthGroupName: Int,
    val maxLengthGroupName: Int,
    val isGroupNameOptional: Boolean,
    val minLengthGroupDescription: Int,
    val maxLengthGroupDescription: Int,
    val isGroupDescriptionOptional: Boolean,
) {
    companion object {
        /**
         * Creates a [GroupsDomainConfig] from a [DomainConfig.GroupRestrictions].
         *
         * @param config The group restrictions configuration to convert.
         * @return A new instance of [GroupsDomainConfig].
         */
        fun from(config: DomainConfig.GroupRestrictions): GroupsDomainConfig =
            GroupsDomainConfig(
                minLengthGroupName = config.name.min,
                maxLengthGroupName = config.name.max,
                isGroupNameOptional = config.name.optional,
                minLengthGroupDescription = config.description.min,
                maxLengthGroupDescription = config.description.max,
                isGroupDescriptionOptional = config.description.optional,
            )
    }
}
