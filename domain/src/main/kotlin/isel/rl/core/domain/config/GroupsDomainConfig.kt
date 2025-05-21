package isel.rl.core.domain.config

data class GroupsDomainConfig(
    val minLengthGroupName: Int,
    val maxLengthGroupName: Int,
    val isGroupNameOptional: Boolean,
    val minLengthGroupDescription: Int,
    val maxLengthGroupDescription: Int,
    val isGroupDescriptionOptional: Boolean,
) {
    companion object {
        fun from(config: DomainConfig.GroupRestrictions): GroupsDomainConfig =
            GroupsDomainConfig(
                minLengthGroupName = config.groupName.min,
                maxLengthGroupName = config.groupName.max,
                isGroupNameOptional = config.groupName.optional,
                minLengthGroupDescription = config.groupDescription.min,
                maxLengthGroupDescription = config.groupDescription.max,
                isGroupDescriptionOptional = config.groupDescription.optional,
            )
    }
}
