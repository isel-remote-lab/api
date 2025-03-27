package rl.domain.config

data class LaboratoryDomainConfig(
    val laboratoryNameSizeInBytes: Int,
) {
    init {
        require(laboratoryNameSizeInBytes > 0) { "Laboratory name size must be greater than zero." }
    }
}
