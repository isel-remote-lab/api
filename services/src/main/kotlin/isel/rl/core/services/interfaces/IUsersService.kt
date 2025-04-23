package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.utils.Either

/**
 * Result of creating a user.
 * It can either be a [Either.Right] with the user ID or a [Either.Left] with an exception.
 */
typealias CreateUserResult = Either<ServicesExceptions, Int>

/**
 * Result of getting a user.
 * It can either be a [Either.Right] with the user or a [Either.Left] with an exception.
 */
typealias GetUserResult = Either<ServicesExceptions, User>

/**
 * Interface for managing users.
 */
interface IUsersService {
    /**
     * Creates a new user.
     * The parameters are validated before creating the user with the user domain.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     *
     * @param oauthId The OAuth ID of the user.
     * @param role The role of the user.
     * @param username The username of the user.
     * @param email The email of the user.
     * @return A result indicating success or failure.
     */
    fun createUser(
        oauthId: String,
        role: String,
        username: String,
        email: String,
    ): CreateUserResult

    /**
     * Retrieves a user by their ID.
     * The ID is validated before retrieving the user from the database.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     * If the user is not found, a [ServicesExceptions.Users.UserNotFound] is returned.
     *
     * @param id The ID of the user.
     * @return A result containing the user or an exception.
     */
    fun getUserById(id: String): GetUserResult

    /**
     * Retrieves a user by their OAuth ID or email.
     * If both are provided, the OAuth ID takes precedence.
     * The parameters are validated before retrieving the user from the database.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     * If the user is not found, a [ServicesExceptions.Users.UserNotFound] is returned.
     *
     * @param oAuthId The OAuth ID of the user.
     * @param email The email of the user.
     * @return A result containing the user or an exception.
     */
    fun getUserByEmailOrAuthId(oAuthId: String? = null, email: String? = null): GetUserResult
}
