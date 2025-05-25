package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.token.Token
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateUserResult
import isel.rl.core.services.interfaces.GetUserResult
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.services.interfaces.LoginUserResult
import isel.rl.core.services.interfaces.UpdateUserRoleResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

/**
 * This class is responsible for managing user-related operations.
 */
@Service
data class UsersService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val usersDomain: UsersDomain,
) : IUsersService {
    override fun login(
        name: String,
        email: String,
    ): LoginUserResult =
        runCatching {
            when (val userResult = getUserByEmail(email)) {
                is Failure ->
                    if (userResult.value == ServicesExceptions.Users.UserNotFound) {
                        createUser(INITIAL_USER_ROLE, name, email).let { result ->
                            if (result is Success) {
                                success(result.value to createToken(result.value.id))
                            } else {
                                failure((result as Failure).value)
                            }
                        }
                    } else {
                        failure(userResult.value)
                    }

                is Success -> success(userResult.value to createToken(userResult.value.id))
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun updateUserRole(
        actorUserId: User,
        targetUserId: String,
        newRole: String?,
    ): UpdateUserRoleResult =
        runCatching {
            // Validate the targetUserId and newRole
            val validatedTargetUserId = usersDomain.validateUserId(targetUserId)
            val validatedNewRole = usersDomain.checkRole(newRole)

            transactionManager.run {
                val usersRepository = it.usersRepository
                usersRepository.getUserById(validatedTargetUserId)
                    ?: return@run failure(ServicesExceptions.Users.UserNotFound)

                if (actorUserId.role != Role.ADMIN) { // Check if the actor is an Admin
                    failure(
                        ServicesExceptions.Forbidden(
                            "Permission denied. Insufficient privileges to update user role.",
                        ),
                    )
                } else if (usersRepository.updateUserRole(validatedTargetUserId, validatedNewRole)) {
                    success(Unit)
                } else {
                    failure(ServicesExceptions.Users.ErrorWhenUpdatingUser)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getUserByTokenValidationInfo(tokenValidationInfo)

            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                revokeToken(token)
                null
            }
        }
    }

    override fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    override fun createUser(
        role: String,
        name: String,
        email: String,
    ): CreateUserResult =
        runCatching {
            val user = usersDomain.validateCreateUser(role, name, email, clock.now())
            transactionManager.run {
                val userId = it.usersRepository.createUser(user)
                success(user.copy(id = userId))
            }
        }.getOrElse { e ->
            // Handle exceptions that may occur during the creation process
            handleException(e as Exception)
        }

    override fun getUserById(id: String): GetUserResult =
        runCatching {
            val validatedId = usersDomain.validateUserId(id)
            transactionManager.run {
                it.usersRepository.getUserById(validatedId)?.let(::success)
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    /**
     * This function retrieves a user by their email address.
     * It validates the email and checks if the user exists in the database.
     * If the user is found, it returns a [success] result with the user data.
     * If the user is not found, it returns a [failure] result with a [ServicesExceptions.Users.UserNotFound] exception.
     *
     * @param email The email address of the user to be retrieved.
     * @return A result containing either the user data or an error.
     */
    override fun getUserByEmail(email: String): GetUserResult =
        runCatching {
            val validatedEmail = usersDomain.checkEmail(email)
            transactionManager.run {
                it.usersRepository.getUserByEmail(validatedEmail)?.let(::success)
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    private fun createToken(userId: Int) =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValue = usersDomain.generateTokenValue()
            val createdAt = clock.now()
            val newToken =
                Token(
                    usersDomain.createTokenValidationInformation(tokenValue),
                    userId,
                    createdAt = createdAt,
                    lastUsedAt = createdAt,
                )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)

            tokenValue
        }

    companion object {
        val INITIAL_USER_ROLE = Role.STUDENT.char
    }
}
