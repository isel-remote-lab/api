package isel.rl.core.domain.config

data class HardwareDomainConfig(
    val minHardwareNameLength: Int,
    val maxHardwareNameLength: Int,
    val isHardwareNameOptional: Boolean,
) {
    companion object {
        /**
         * Creates a [HardwareDomainConfig] from a [DomainConfig.HardwareRestrictions].
         *
         * @param config The hardware restrictions configuration to convert.
         * @return A new instance of [HardwareDomainConfig].
         */
        fun from(config: DomainConfig.HardwareRestrictions): HardwareDomainConfig =
            HardwareDomainConfig(
                minHardwareNameLength = config.name.min,
                maxHardwareNameLength = config.name.max,
                isHardwareNameOptional = config.name.optional,
            )
    }
}
