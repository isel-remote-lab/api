package rl.services.interfaces

import rl.domain.exceptions.ServicesExceptions
import rl.domain.user.User
import rl.services.utils.Either

typealias CreateUserResult = Either<ServicesExceptions, Int>
typealias GetUserResult = Either<ServicesExceptions, User>

interface IUsersServices {
    fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String
    ): CreateUserResult

    fun getUserById(
        id: String
    ): GetUserResult

    fun getUserByEmail(
        email: String
    ): GetUserResult
}