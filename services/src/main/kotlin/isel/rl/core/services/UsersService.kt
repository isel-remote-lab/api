package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.security.JWTUtils
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
    private val jwtUtils: JWTUtils,
    private val clock: Clock,
) : IUsersService {
    override fun login(
        oauthId: String,
        username: String,
        email: String,
        accessToken: String,
    ): LoginUserResult =
        try {
            when (val getByOauthRes = getUserByOAuthId(oauthId)) {
                is Failure -> {
                    if (getByOauthRes.value == ServicesExceptions.Users.UserNotFound) {
                        val createUserResult = createUser(oauthId, INITIAL_USER_ROLE, username, email)

                        if (createUserResult is Success) {
                            success(
                                jwtUtils.generateJWTToken(
                                    createUserResult.value.toString(),
                                    oauthId,
                                    INITIAL_USER_ROLE,
                                    username,
                                    email,
                                    accessToken,
                                    clock.now(),
                                ),
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
                        jwtUtils.generateJWTToken(
                            user.id.toString(),
                            user.oAuthId.oAuthIdInfo,
                            user.role.char,
                            user.username.usernameInfo,
                            user.email.emailInfo,
                            accessToken,
                            clock.now(),
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            // Handle exceptions that may occur during the login process
            handleException(e)
        }

    override fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
    ): CreateUserResult =
        try {
            // Validate the user data
            val user =
                usersDomain.validateCreateUser(
                    oauthId,
                    role,
                    username,
                    email,
                    clock.now(),
                )
            transactionManager.run {
                // Create the user in the database and return the result as success
                success(
                    it.usersRepository.createUser(user),
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

    override fun getUserByEmailOrAuthId(
        oAuthId: String?,
        email: String?,
    ): GetUserResult {
        // Validate the input parameters
        if (oAuthId == null && email == null) {
            return failure(ServicesExceptions.Users.InvalidQueryParams)
        }

        // Check if the OAuth ID is provided and retrieve the user by OAuth ID
        // Otherwise, retrieve the user by email
        return if (oAuthId != null) {
            getUserByOAuthId(oAuthId)
        } else {
            getUserByEmail(email!!)
        }
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
    private fun getUserByEmail(email: String): GetUserResult =
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

    /**
     * This function retrieves a user by their OAuthID.
     * It validates the OAuth ID and checks if the user exists in the database.
     * If the user is found, it returns a [success] result with the user data.
     * If the user is not found, it returns a [failure] result with a [ServicesExceptions.Users.UserNotFound] exception.
     *
     * @param oauthId The OAuth ID of the user to be retrieved.
     * @return A result containing either the user data or an error.
     */
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

    companion object {
        const val INITIAL_USER_ROLE = "S"
    }
}
