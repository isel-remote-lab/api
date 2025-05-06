package isel.rl.core.domain

data class LimitAndSkip(
    val limit: Int = DEFAULT_LIMIT,
    val skip: Int = DEFAULT_SKIP,
) {
    companion object {
        const val DEFAULT_LIMIT = 30
        const val DEFAULT_SKIP = 0
    }
}
