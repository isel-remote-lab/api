package isel.rl.core.domain.group

import isel.rl.core.domain.user.User

data class GroupWithUsers(
    val group: Group,
    val users: List<User>,
)
