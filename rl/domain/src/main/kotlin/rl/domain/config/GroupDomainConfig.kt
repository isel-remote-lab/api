package rl.domain.config

data class GroupDomainConfig(
    val groupNameSizeInBytes: Int,
    val groupDescriptionSizeInBytes: Int,
) {
    init {
        require(groupNameSizeInBytes > 0) { "groupNameSizeInBytes must be positive" }
        require(groupDescriptionSizeInBytes > 0) { "groupDescriptionSizeInBytes must be positive" }
    }
}
