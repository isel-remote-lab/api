package rl.http

object Uris {
    const val PREFIX = "/api/v1"

    object Users {
        const val CREATE = "$PREFIX/users" // URI for creating a user
        const val GET = "$PREFIX/users/{id}" // URI for getting a user by ID
        const val GET_BY_EMAIL = "$PREFIX/users" // URI for getting all users
    }
}
