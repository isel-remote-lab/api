package repositoryJdbi

import org.jdbi.v3.core.Handle

data class JdbiUserRepository(
    val handle: Handle
) {
}