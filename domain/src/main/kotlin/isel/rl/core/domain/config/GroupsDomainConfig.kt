package isel.rl.core.domain.config

data class GroupsDomainConfig(
    val minLengthGroupName: Int,
    val maxLengthGroupName: Int,
    val minLengthGroupDescription: Int,
    val maxLengthGroupDescription: Int,
)
