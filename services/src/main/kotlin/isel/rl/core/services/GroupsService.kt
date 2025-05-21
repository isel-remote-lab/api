package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.GroupWithUsers
import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.AddUserToGroupResult
import isel.rl.core.services.interfaces.CreateGroupResult
import isel.rl.core.services.interfaces.GetGroupByIdResult
import isel.rl.core.services.interfaces.GetUserGroupsResult
import isel.rl.core.services.interfaces.IGroupsService
import isel.rl.core.services.interfaces.RemoveUserFromGroupResult
import isel.rl.core.services.utils.handleException
import isel.rl.core.services.utils.verifyQuery
import isel.rl.core.utils.failure
import isel.rl.core.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
class GroupsService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val groupsDomain: GroupsDomain,
    private val usersDomain: UsersDomain,
) : IGroupsService {
    override fun createGroup(
        groupName: String?,
        groupDescription: String?,
        ownerId: Int,
    ): CreateGroupResult =
        try {
            val group =
                groupsDomain.validateCreateGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    createdAt = clock.now(),
                    ownerId = ownerId,
                )

            transactionManager.run {
                val groupId = it.groupsRepository.createGroup(group)

                return@run success(
                    Group(
                        groupId,
                        group.groupName,
                        group.groupDescription,
                        group.createdAt,
                        group.ownerId,
                    ),
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }

    override fun getGroupById(groupId: String): GetGroupByIdResult =
        try {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)

            transactionManager.run {
                val group = it.groupsRepository.getGroupById(validatedGroupId)
                if (group != null) {
                    // Get group users
                    val usersIds = it.groupsRepository.getGroupUsers(validatedGroupId)
                    val usersList =
                        usersIds.map { userId ->
                            it.usersRepository.getUserById(userId)!!
                        }

                    return@run success(
                        GroupWithUsers(
                            group,
                            usersList,
                        ),
                    )
                } else {
                    return@run failure(ServicesExceptions.Groups.GroupNotFound)
                }
            }
        } catch (e: Exception) {
            handleException(e)
        }

    override fun getUserGroups(
        userId: String,
        limit: String?,
        skip: String?,
    ): GetUserGroupsResult {
        return try {
            // Validate limit and skip
            val limitAndSkip = verifyQuery(limit, skip)

            val validatedUserId = usersDomain.validateUserId(userId)

            transactionManager.run {
                if (!it.usersRepository.checkIfUserExists(validatedUserId)) {
                    return@run failure(ServicesExceptions.Users.UserNotFound)
                }

                return@run success(it.groupsRepository.getUserGroups(validatedUserId, limitAndSkip))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun addUserToGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): AddUserToGroupResult {
        return try {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)
            val validatedUserId =
                userId?.let(usersDomain::validateUserId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("UserId cannot be null"))

            transactionManager.run {
                val groupRepo = it.groupsRepository
                val userRepo = it.usersRepository

                when {
                    !userRepo.checkIfUserExists(validatedUserId) -> failure(ServicesExceptions.Users.UserNotFound)
                    !groupRepo.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    groupRepo.getGroupOwnerId(validatedGroupId) != actorUserId ->
                        failure(ServicesExceptions.Groups.GroupNotFound) // Safety reasons
                    groupRepo.getGroupUsers(validatedGroupId)
                        .contains(validatedUserId) -> failure(ServicesExceptions.Groups.UserAlreadyInGroup)

                    groupRepo.addUserToGroup(validatedUserId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun removeUserFromGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): RemoveUserFromGroupResult {
        return try {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)
            val validatedUserId =
                userId?.let(usersDomain::validateUserId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("UserId cannot be null"))

            transactionManager.run {
                val groupRepo = it.groupsRepository
                val userRepo = it.usersRepository

                when {
                    !userRepo.checkIfUserExists(validatedUserId) -> failure(ServicesExceptions.Users.UserNotFound)
                    !groupRepo.checkIfGroupExists(validatedGroupId) -> failure(ServicesExceptions.Groups.GroupNotFound)
                    groupRepo.getGroupOwnerId(validatedGroupId) != actorUserId ->
                        failure(ServicesExceptions.Groups.GroupNotFound) // Safety reasons

                    !groupRepo.getGroupUsers(validatedGroupId)
                        .contains(validatedUserId) -> failure(ServicesExceptions.Groups.UserNotInGroup)

                    groupRepo.removeUserFromGroup(validatedUserId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }
}
