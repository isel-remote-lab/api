package isel.rl.core.domain.config

import kotlin.time.Duration

/**
 * Configuration for user management in the domain.
 *
 * @property tokenSizeInBytes The size of the token in bytes.
 * @property tokenTtl The time-to-live for the token.
 * @property tokenRollingTtl The rolling time-to-live for the token.
 * @property maxTokensPerUser The maximum number of tokens allowed per user.
 */
data class UsersDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: Int,
)
