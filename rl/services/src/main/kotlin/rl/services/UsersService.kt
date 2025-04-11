package rl.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import rl.domain.exceptions.ServicesExceptions
import rl.domain.user.domain.UsersDomain
import rl.repository.TransactionManager
import rl.services.interfaces.CreateUserResult
import rl.services.interfaces.GetUserResult
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
                val usersRepo = it.usersRepository
                return@run success(
                    usersRepo.createUser(user)
                )
            }
        } catch (e: Exception) {
            return when (e) {
                is ServicesExceptions -> failure(e)
                else -> failure(ServicesExceptions.UnexpectedError)
            }
        }
    }

    override fun getUserById(id: String): GetUserResult {
        try {
            val validatedId = usersDomain.validateUserId(id)

            return transactionManager.run {
                val usersRepo = it.usersRepository
                return@run usersRepo.getUserById(validatedId)
                    ?.let { user -> success(user) }
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            return when (e) {
                is ServicesExceptions -> failure(e)
                else -> failure(ServicesExceptions.UnexpectedError)
            }
        }
    }

    override fun getUserByEmail(email: String): GetUserResult {
        try {
            val validatedEmail = usersDomain.checkEmail(email)

            return transactionManager.run {
                val usersRepo = it.usersRepository
                return@run usersRepo.getUserByEmail(validatedEmail)
                    ?.let { user -> success(user) }
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            return when (e) {
                is ServicesExceptions -> failure(e)
                else -> failure(ServicesExceptions.UnexpectedError)
            }
        }
    }

}


