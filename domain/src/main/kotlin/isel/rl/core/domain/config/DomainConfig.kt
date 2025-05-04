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
        val minLengthLabName: Int,
        val maxLengthLabName: Int,
        val minLengthLabDescription: Int,
        val maxLengthLabDescription: Int,
        val minLabDuration: Int,
        val maxLabDuration: Int,
        val labDurationUnit: String,
        val minLabQueueLimit: Int,
        val maxLabQueueLimit: Int,
    )

    @Serializable
    data class GroupRestrictions(
        val minLengthGroupName: Int,
        val maxLengthGroupName: Int,
        val minLengthGroupDescription: Int,
        val maxLengthGroupDescription: Int
    )

    companion object {
        fun parseDomainConfigs(jsonString: String): DomainConfig {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<DomainConfig>(jsonString)
        }
    }
}