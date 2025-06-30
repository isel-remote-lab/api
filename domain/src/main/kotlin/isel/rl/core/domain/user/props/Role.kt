package isel.rl.core.domain.user.props

enum class Role(val char: String) {
    STUDENT("S"),
    TEACHER("T"),
    ADMIN("A");

    companion object {
        fun checkHierarchyPermission(
            userRole: Role,
            requiredRole: Role,
        ): Boolean {
            return when (userRole) {
                STUDENT -> requiredRole == STUDENT
                TEACHER -> requiredRole == STUDENT || requiredRole == TEACHER
                ADMIN -> true // Admin has access to everything
            }
        }
    }
}
