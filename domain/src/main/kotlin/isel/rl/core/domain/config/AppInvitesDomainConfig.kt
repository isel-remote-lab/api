package isel.rl.core.domain.config

import kotlin.time.Duration

data class AppInvitesDomainConfig(
    val inviteCodeSizeInBytes: Int,
    val inviteTtl: Duration,
    val maxInvitesPerUser: Int,
    val maxTimesUsedPerInvite: Int,
) {
    init {
        require(inviteCodeSizeInBytes > 0) { "inviteCodeSizeInBytes must be positive" }
        require(inviteTtl.isPositive()) { "inviteTtl must be positive" }
        require(maxInvitesPerUser > 0) { "maxInvitesPerUser must be positive" }
        require(maxTimesUsedPerInvite > 0) { "maxTimesUsedPerInvite must be positive" }
    }
}
