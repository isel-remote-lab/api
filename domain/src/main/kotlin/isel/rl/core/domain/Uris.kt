package isel.rl.core.domain

object Uris {
    /**
     * Prefix of the URI
     */
    private const val API_PREFIX = "/api"
    private const val API_VERSION = "v1"
    private const val PREFIX = "$API_PREFIX/$API_VERSION"
    private const val PREFIX_PRIVATE = "/api/v1/_private"

    object Auth {
        private const val BASE = "$PREFIX_PRIVATE/auth"

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
        private const val BASE = "$PREFIX/users"

        /**
         * URI for getting a user by ID
         */
        const val GET = "$BASE/{id}"

        /**
         * URI for getting a user by email or oAuth id with query parameters
         */
        const val GET_BY_EMAIL = BASE

        /**
         * URI for updating a user role by ID
         */
        const val UPDATE_USER_ROLE = "$BASE/{id}/role"
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
        const val GET_BY_ID = "$BASE/{id}"

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
        private const val BASE = "$PREFIX/groups"

        /**
         * URI for creating a group
         */
        const val CREATE = BASE

        /**
         * URI for getting a group by ID
         */
        const val GET_BY_ID = "$BASE/{id}"

        /**
         * URI for getting all groups of a user
         */
        const val GET_USER_GROUPS = BASE

        /**
         * URI for adding a user to a group
         */
        const val ADD_USER_TO_GROUP = "$BASE/groups/{id}/users"

        /**
         * URI for removing a user from a group
         */
        const val REMOVE_USER_FROM_GROUP = "$BASE/groups/{id}/users"

        /**
         * URI for getting all users of a group
         */
        const val DELETE = "$BASE/{id}"
    }

    object Private {
        /**
         * URI for getting the domain configuration
         */
        const val GET_DOMAIN = "$PREFIX_PRIVATE/domain"
    }
}
