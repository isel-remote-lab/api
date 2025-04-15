package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.utils.Either

typealias CreateUserResult = Either<ServicesExceptions, Int>
typealias GetUserResult = Either<ServicesExceptions, User>

interface IUsersServices {
    fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
    ): CreateUserResult

    fun getUserById(id: String): GetUserResult

    fun getUserByEmail(email: String): GetUserResult

    fun getUserByOAuthId(oauthId: String): GetUserResult
}
