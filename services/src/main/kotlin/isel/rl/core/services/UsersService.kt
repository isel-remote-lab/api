package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateUserResult
import isel.rl.core.services.interfaces.GetUserResult
import isel.rl.core.services.interfaces.IUsersServices
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
data class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) : IUsersServices {
    override fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
    ): CreateUserResult {
        try {
            val user =
                usersDomain.validateCreateUser(
                    oauthId,
                    role,
                    username,
                    email,
                    clock.now(),
                )
            return transactionManager.run {
                val usersRepo = it.usersRepository
                return@run success(
                    usersRepo.createUser(user),
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

    override fun getUserByOAuthId(oauthId: String): GetUserResult {
        try {
            val validatedOAuthId = usersDomain.checkOAuthId(oauthId)

            return transactionManager.run {
                val usersRepo = it.usersRepository
                return@run usersRepo.getUserByOAuthId(validatedOAuthId)
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
