package isel.rl.core.domain.user.domain

import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import kotlinx.datetime.Instant

data class ValidatedUser internal constructor(
    val role: Role,
    val name: Name,
    val email: Email,
    val createdAt: Instant,
)
