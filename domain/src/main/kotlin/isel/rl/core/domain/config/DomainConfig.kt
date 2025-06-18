package isel.rl.core.domain.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Data class representing the domain configuration. Using the [Serializable] annotation this class can be serialized
 * to and deserialized from JSON.
 * It contains restrictions for users, laboratories, and groups, each represented by their own data class, also using the
 * [Serializable] annotation.
 */
@Serializable
data class DomainConfig(
    val user: UserRestrictions,
    val laboratory: LaboratoryRestrictions,
    val group: GroupRestrictions,
    val hardware: HardwareRestrictions
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
        val name: Properties,
        val description: Properties,
        val duration: Properties,
        val queueLimit: Properties,
    )

    @Serializable
    data class GroupRestrictions(
        val name: Properties,
        val description: Properties,
    )

    @Serializable
    data class HardwareRestrictions(
        val name: Properties
    )

    @Serializable
    data class Properties(
        val min: Int = 0,
        val max: Int = 0,
        val optional: Boolean = false,
        val unit: String = "",
    )

    companion object {
        /**
         * Parses a JSON string into a [DomainConfig] object.
         *
         * @param jsonString The JSON string to parse.
         * @return A [DomainConfig] object representing the parsed configuration.
         */
        fun parseDomainConfigs(jsonString: String): DomainConfig {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<DomainConfig>(jsonString)
        }
    }
}
