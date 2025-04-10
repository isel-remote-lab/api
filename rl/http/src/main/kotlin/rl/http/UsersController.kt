package rl.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import rl.http.model.Problem
import rl.http.model.UserCreateInputModel
import rl.services.interfaces.CreateUserError
import rl.services.utils.Failure
import rl.services.interfaces.IUsersServices
import rl.services.utils.Success

@RestController
data class UsersController(
    private val usersService: IUsersServices,
) {
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        return when (val result = usersService.createUser(input.oauthId, input.role, input.username, input.email)) {
            is Success -> {
                ResponseEntity.status(201).body(result.value)
            }

            is Failure -> {
                when (result.value) {
                    CreateUserError.UnexpectedError -> Problem.response(500, Problem.unexpectedBehaviour)
                    CreateUserError.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
                    CreateUserError.InvalidRole -> Problem.response(400, Problem.invalidRole)
                    CreateUserError.InvalidUsername -> Problem.response(400, Problem.invalidUsername)
                    CreateUserError.InvalidOauthId -> Problem.response(400, Problem.invalidOauthId)
                }
            }
        }
    }
}
