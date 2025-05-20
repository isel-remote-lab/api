package isel.rl.core.domain.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DomainConfig(
    val user: UserRestrictions,
    val laboratory: LaboratoryRestrictions,
    val group: GroupRestrictions,
) {
    @Serializable
    data class UserRestrictions(
        val tokenSizeInBytes: Int,
        val tokenTtl: Int,
        val tokenRollingTtl: Int,
        val tokenTtlDurationUnit: String,
        val maxTokensPerUser: Int,
    )

    @Serializable
    data class LaboratoryRestrictions(
        val labName: Properties,
        val labDescription: Properties,
        val labDuration: Properties,
        val labQueueLimit: Properties,
    )

    @Serializable
    data class GroupRestrictions(
        val groupName: Properties,
        val groupDescription: Properties,
    )

    @Serializable data class Properties(
        val min: Int = 0,
        val max: Int = 0,
        val optional: Boolean = false,
        val unit: String = "",
    )

    companion object {
        fun parseDomainConfigs(jsonString: String): DomainConfig {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<DomainConfig>(jsonString)
        }
    }
}
