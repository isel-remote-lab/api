package isel.rl.core.domain

object Uris {
    /**
     * Prefix of the URI
     */
    //private const val PREFIX = "/api/v1"

    object Auth {
        private const val BASE = "/auth"

        /**
         * URI for login
         */
        const val LOGIN = "$BASE/login"

        /**
         * URI for logout
         */
        const val LOGOUT = "$BASE/logout"
    }

    object Users {
        private const val BASE = "/users"

        /**
         * URI for getting a user by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for getting a user by email or oAuth id with query parameters
         */
        const val GET_BY_EMAIL = BASE
    }

    object Laboratories {
        private const val BASE = "/laboratories"

        /**
         * URI for creating a laboratory
         */
        const val CREATE = BASE

        /**
         * URI for getting a laboratory by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for getting all laboratories of the authenticated user
         */
        const val GET_ALL_BY_USER = BASE

        /**
         * URI for updating a laboratory by ID
         */
        const val UPDATE = "$BASE/{id}"

        /**
         * URI for deleting a laboratory by ID
         */
        const val DELETE = "$BASE/{id}"

        /**
         * URI for getting all laboratories
         */
        const val GET_ALL = BASE
    }

    object Groups {
        private const val BASE = "/groups"

        /**
         * URI for creating a group
         */
        const val CREATE = BASE

        /**
         * URI for getting a group by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for getting all groups of the authenticated user
         */
        const val GET_ALL_BY_USER = BASE
    }
}
