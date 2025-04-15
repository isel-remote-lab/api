package isel.rl.core.domain.config

data class HardwareDomainConfig(
    val hardwareNameSizeInBytes: Int,
) {
    init {
        require(hardwareNameSizeInBytes > 0) { "HardwareNameSizeInBytes must be greater than zero." }
    }
}
