package isel.rl.core.repository.utils

/**
 * Environment object that provides methods to access environment variables.
 */
object Environment {
    /**
     * Retrieves the database URL from the environment variables.
     *
     * @return the database URL
     * @throws Exception if the environment variable is not set
     */
    fun getDbUrl() = System.getenv(KEY_DB_URL) ?: throw Exception("Missing env var $KEY_DB_URL")

    /**
     * The key for the database URL environment variable.
     */
    private const val KEY_DB_URL = "DB_URL"
}
