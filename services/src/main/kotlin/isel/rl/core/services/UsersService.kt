package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.domain.user.token.Token
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.CreateUserResult
import isel.rl.core.services.interfaces.GetUserResult
import isel.rl.core.services.interfaces.IUsersService
import isel.rl.core.services.interfaces.LoginUserResult
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
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) : IUsersService {
    override fun login(
        username: String,
        email: String,
    ): LoginUserResult =
        try {
            when (val getByOauthRes = getUserByEmail(email)) {
                is Failure -> {
                    if (getByOauthRes.value == ServicesExceptions.Users.UserNotFound) {
                        val createUserResult = createUser(INITIAL_USER_ROLE, username, email)

                        if (createUserResult is Success) {
                            success(
                                createUserResult.value to createToken(createUserResult.value.id),
                            )
                        } else {
                            failure((createUserResult as Failure).value)
                        }
                    } else {
                        failure(getByOauthRes.value)
                    }
                }

                is Success -> {
                    val user = getByOauthRes.value
                    success(
                        user to createToken(user.id),
                    )
                }
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the login process
            handleException(e)
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
        username: String,
        email: String,
    ): CreateUserResult =
        try {
            // Validate the user data
            val user =
                usersDomain.validateCreateUser(
                    role,
                    username,
                    email,
                    clock.now(),
                )
            transactionManager.run {
                val userId = it.usersRepository.createUser(user)
                // Create the user in the database and return the result as success
                success(
                    User(
                        id = userId,
                        role = user.role,
                        username = user.username,
                        email = user.email,
                        createdAt = user.createdAt,
                    ),
                )
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the creation process
            handleException(e)
        }

    override fun getUserById(id: String): GetUserResult =
        try {
            // Validate the user ID
            val validatedId = usersDomain.validateUserId(id)

            transactionManager.run {
                // Retrieve the user from the database and return the result as success
                // or failure if the user is not found
                it.usersRepository.getUserById(validatedId)
                    ?.let(::success)
                    ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the retrieval process
            handleException(e)
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
        try {
            // Validate the email
            val validatedEmail = usersDomain.checkEmail(email)

            transactionManager.run {
                // Retrieve the user by email from the database and return the result as success
                it.usersRepository.getUserByEmail(validatedEmail)
                    ?.let(::success) ?: failure(ServicesExceptions.Users.UserNotFound)
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the retrieval process
            handleException(e)
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
        const val INITIAL_USER_ROLE = "S"
    }
}
