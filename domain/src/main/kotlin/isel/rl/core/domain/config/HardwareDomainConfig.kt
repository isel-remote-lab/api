package isel.rl.core.domain.config

data class HardwareDomainConfig(
    val minHardwareNameLength: Int,
    val maxHardwareNameLength: Int,
    val isHardwareNameOptional: Boolean,
    val minHardwareStatusLength: Int,
    val maxHardwareStatusLength: Int,
    val hardwareStatusOptionsAvailable: List<String>,
    val isHardwareStatusOptional: Boolean,
    val minMacAddressLength: Int,
    val maxMacAddressLength: Int,
    val isMacAddressOptional: Boolean,
    val minIpAddressLength: Int,
    val maxIpAddressLength: Int,
    val isIpAddressOptional: Boolean,
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
                minHardwareStatusLength = config.name.min,
                maxHardwareStatusLength = config.name.max,
                hardwareStatusOptionsAvailable = config.status.availableOptions,
                isHardwareStatusOptional = config.name.optional,
                minMacAddressLength = config.name.min,
                maxMacAddressLength = config.name.max,
                isMacAddressOptional = config.name.optional,
                minIpAddressLength = config.name.min,
                maxIpAddressLength = config.name.max,
                isIpAddressOptional = config.name.optional,
            )
    }
}
