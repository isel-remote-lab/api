package isel.rl.core.domain.user

import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import kotlinx.datetime.Instant

class User(
    val id: Int = 0,
    val role: Role,
    val name: Name,
    val email: Email,
    val createdAt: Instant,
) {
    companion object {
        const val ID_PROP = "id"
        const val ROLE_PROP = "role"
        const val NAME_PROP = "name"
        const val EMAIL_PROP = "email"
        const val CREATED_AT_PROP = "created_at"
    }
}
