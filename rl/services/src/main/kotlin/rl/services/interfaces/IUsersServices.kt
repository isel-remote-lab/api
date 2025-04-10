package rl.services.interfaces

import rl.services.utils.Either

sealed class CreateUserError {
    data object InvalidOauthId : CreateUserError()
    data object InvalidRole : CreateUserError()
    data object InvalidUsername : CreateUserError()
    data object InvalidEmail : CreateUserError()

    data object UnexpectedError : CreateUserError()
}

typealias CreateUserResult = Either<CreateUserError, Int>

interface IUsersServices {
    fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String
    ): CreateUserResult
}