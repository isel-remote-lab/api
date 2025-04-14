package rl.http

object Uris {
    /**
     * Prefix of the URI
     */
    const val PREFIX = "/api/v1"

    object Users {
        /**
         * URI for creating a user
         */
        const val CREATE = "$PREFIX/users"

        /**
         * URI for getting a user by ID
         */
        const val GET = "$PREFIX/users/{id}"

        /**
         * URI for getting all users
         */
        const val GET_BY_EMAIL = "$PREFIX/users"

        /**
         * URI for getting users by oAuthId
         */
        const val GET_BY_OAUTHID = "$PREFIX/users/{oauthid}"
    }
}
