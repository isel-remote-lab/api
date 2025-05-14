package isel.rl.core.services.interfaces

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.User
import isel.rl.core.utils.Either

typealias AuthToken = String
typealias UserAndToken = Pair<User, AuthToken>
typealias LoginUserResult = Either<ServicesExceptions, UserAndToken>
typealias UpdateUserRoleResult = Either<ServicesExceptions, Boolean>

/**
 * Result of creating a user.
 * It can either be a [Either.Right] with the user ID or a [Either.Left] with an exception.
 */
typealias CreateUserResult = Either<ServicesExceptions, User>

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
     * Logs in a user.
     * If the user does not exist, it creates a new user.
     * The parameters are validated before creating the user with the user domain.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     *
     * @param name The username of the user.
     * @param email The email of the user.
     * @return A result indicating success or failure.
     */
    fun login(
        name: String,
        email: String,
    ): LoginUserResult

    /**
     * Updates the role of a user.
     * The parameters are validated before updating the user with the user domain.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     *
     * @param actorUserId The ID of the user performing the action.
     * @param targetUserId The ID of the user whose role is being updated.
     * @param newRole The new role to assign to the user.
     * @return A result indicating success or failure.
     */
    fun updateUserRole(
        actorUserId: User,
        targetUserId: String,
        newRole: String?,
    ): UpdateUserRoleResult

    /**
     * Retrieves a user by token.
     *
     * @param token the token of the user
     * @return the User if found, or null otherwise
     */
    fun getUserByToken(token: String): User?

    /**
     * Revokes a token.
     *
     * @param token the token to revoke
     * @return true if the token was successfully revoked, false otherwise
     */
    fun revokeToken(token: String): Boolean

    /**
     * Creates a new user.
     * The parameters are validated before creating the user with the user domain.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     *
     * @param role The role of the user.
     * @param name The username of the user.
     * @param email The email of the user.
     * @return A result indicating success or failure.
     */
    fun createUser(
        role: String,
        name: String,
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
     * Retrieves a user by their email.
     * The email is validated before retrieving the user from the database.
     * If the validation fails, a [ServicesExceptions] is returned as failure.
     * If the user is not found, a [ServicesExceptions.Users.UserNotFound] is returned.
     *
     * @param email The email of the user.
     * @return A result containing the user or an exception.
     */
    fun getUserByEmail(email: String): GetUserResult
}
