package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateUserResult
import isel.rl.core.services.interfaces.GetUserResult
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
data class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) : IUsersService {
    override fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
    ): CreateUserResult =
        try {
            val user =
                usersDomain.validateCreateUser(
                    oauthId,
                    role,
                    username,
                    email,
                    clock.now(),
                )
            transactionManager.run {
                success(
                    it.usersRepository.createUser(user),
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }


    override fun getUserById(id: String): GetUserResult =
        try {
            val validatedId = usersDomain.validateUserId(id)

            transactionManager.run {
                it.usersRepository.getUserById(validatedId)
                    ?.let(::success)
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            handleException(e)
        }

    override fun getUserByEmailOrAuthId(oAuthId: String?, email: String?): GetUserResult {
        // Check for oauthId first, then email
        // If both are provided, prefer oauthId
        if (oAuthId == null && email == null) {
            return failure(ServicesExceptions.Users.InvalidQueryParams)
        }

        return if(oAuthId != null)
            getUserByOAuthId(oAuthId)
        else
            getUserByEmail(email!!)
    }

    private fun getUserByEmail(email: String): GetUserResult =
        try {
            val validatedEmail = usersDomain.checkEmail(email)
            transactionManager.run {
                it.usersRepository.getUserByEmail(validatedEmail)
                    ?.let(::success) ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            handleException(e)
        }

    private fun getUserByOAuthId(oauthId: String): GetUserResult =
        try {
            val validatedOAuthId = usersDomain.checkOAuthId(oauthId)
            transactionManager.run {
                it.usersRepository.getUserByOAuthId(validatedOAuthId)
                    ?.let(::success) ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            handleException(e)
        }
}
