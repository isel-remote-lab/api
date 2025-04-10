package rl.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import rl.domain.user.domain.UsersDomain
import rl.domain.user.domain.UsersDomainException
import rl.repository.TransactionManager
import rl.services.interfaces.CreateUserError
import rl.services.interfaces.CreateUserResult
import rl.services.interfaces.IUsersServices
import rl.services.utils.failure
import rl.services.utils.success

@Named
data class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock
) : IUsersServices {
    override fun createUser(oauthId: String, role: String, username: String, email: String): CreateUserResult {
        try {
            val user = usersDomain.validateCreateUser(
                oauthId,
                role,
                username,
                email,
                clock.now()
            )

            return transactionManager.run {
                val usersRepository = it.usersRepository
                return@run success(
                    usersRepository.createUser(user)
                )
            }
        } catch (e: Exception) {
            return when (e) {
                is UsersDomainException.InvalidRole -> failure(CreateUserError.InvalidRole)
                is UsersDomainException.InvalidOauthId -> failure(CreateUserError.InvalidOauthId)
                is UsersDomainException.InvalidUsername -> failure(CreateUserError.InvalidUsername)
                is UsersDomainException.InvalidEmail -> failure(CreateUserError.InvalidEmail)
                else -> failure(CreateUserError.UnexpectedError)
            }
        }
    }
}


