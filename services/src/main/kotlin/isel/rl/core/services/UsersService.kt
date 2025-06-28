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
import org.slf4j.LoggerFactory
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
            LOG.info("Login attempt for email: {}", email)

            when (val userResult = getUserByEmail(email)) {
                is Failure -> {
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
                }

                is Success -> {
                    success(userResult.value to createToken(userResult.value.id))
                }
            }
        }.getOrElse { e ->
            LOG.error("Unexpected error during login for email: {}. Exception: {}", email, e.message, e)
            handleException(e as Exception)
        }

    override fun updateUserRole(
        actorUserId: User,
        targetUserId: String,
        newRole: String?,
    ): UpdateUserRoleResult =
        runCatching {
            LOG.info(
                "User {} attempting to update role for user {} to role: {}",
                actorUserId.id,
                targetUserId,
                newRole,
            )

            // Validate the targetUserId and newRole
            val validatedTargetUserId = usersDomain.validateUserId(targetUserId)
            val validatedNewRole = usersDomain.checkRole(newRole)

            transactionManager.run {
                val usersRepository = it.usersRepository
                val targetUser = usersRepository.getUserById(validatedTargetUserId)

                if (targetUser == null) {
                    return@run failure(ServicesExceptions.Users.UserNotFound)
                }

                if (usersRepository.updateUserRole(validatedTargetUserId, validatedNewRole)) {
                    success(Unit)
                } else {
                    failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            LOG.error(
                "Unexpected error during role update. Actor: {}, Target: {}, New Role: {}. Exception: {}",
                actorUserId.id,
                targetUserId,
                newRole,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun getUserByToken(token: String): User? {
        LOG.debug("Attempting to get user by token")

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
        LOG.debug("Revoking token")
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
            LOG.info("Creating new user with name: {}, email: {}, role: {}", name, email, role)

            val user = usersDomain.validateCreateUser(role, name, email, clock.now())

            transactionManager.run {
                val userId = it.usersRepository.createUser(user)
                success(user.copy(id = userId))
            }
        }.getOrElse { e ->
            LOG.error(
                "Failed to create user with name: {}, email: {}, role: {}. Exception: {}",
                name,
                email,
                role,
                e.message,
                e,
            )
            handleException(e as Exception)
        }

    override fun getUserById(id: String): GetUserResult =
        runCatching {
            LOG.info("Getting user by ID: {}", id)

            val validatedId = usersDomain.validateUserId(id)
            transactionManager.run {
                val user = it.usersRepository.getUserById(validatedId)
                if (user != null) {
                    success(user)
                } else {
                    failure(ServicesExceptions.Users.UserNotFound)
                }
            }
        }.getOrElse { e ->
            LOG.error("Error getting user by ID: {}. Exception: {}", id, e.message, e)
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
            LOG.info("Getting user by email: {}", email)

            val validatedEmail = usersDomain.checkEmail(email)
            transactionManager.run {
                val user = it.usersRepository.getUserByEmail(validatedEmail)
                if (user != null) {
                    success(user)
                } else {
                    failure(ServicesExceptions.Users.UserNotFound)
                }
            }
        }.getOrElse { e ->
            LOG.error("Error getting user by email: {}. Exception: {}", email, e.message, e)
            handleException(e as Exception)
        }

    private fun createToken(userId: Int): String {
        return transactionManager.run {
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
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UsersService::class.java)
        val INITIAL_USER_ROLE = Role.STUDENT.char
    }
}
