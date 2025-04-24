package isel.rl.core.domain

object Uris {
    /**
     * Prefix of the URI
     */
    private const val PREFIX = "/api/v1"
    private const val PRIVATE = "$PREFIX/_private"

    object Users {
        private const val BASE = "$PREFIX/users"
        private const val BASE_PRIVATE = "$PRIVATE/users"

        /**
         * URI for creating a user
         */
        const val CREATE = BASE_PRIVATE

        /**
         * URI for login
         */
        const val LOGIN = "$BASE_PRIVATE/login"

        /**
         * URI for getting a user by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for getting a user by email or oAuth id with query parameters
         */
        const val GET_BY_EMAIL = BASE

        const val GET_BY_OAUTHID = BASE_PRIVATE
    }

    object Laboratories {
        private const val BASE = "$PREFIX/laboratories"

        /**
         * URI for creating a laboratory
         */
        const val CREATE = BASE

        /**
         * URI for getting a laboratory by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for updating a laboratory by ID
         */
        const val UPDATE = "$BASE/{id}"

        /**
         * URI for getting all laboratories
         */
        const val GET_ALL = BASE
    }
}
