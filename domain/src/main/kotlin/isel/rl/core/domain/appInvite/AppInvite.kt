package isel.rl.core.domain.appInvite

import kotlinx.datetime.Instant

data class AppInvite(
    val id: Int,
    val inviteCodeInfo: InviteCodeInfo,
    val ownerId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val groupId: Int,
)
