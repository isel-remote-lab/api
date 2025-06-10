package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.group.Group
import isel.rl.core.domain.group.domain.GroupsDomain
import isel.rl.core.domain.user.User
import isel.rl.core.domain.user.domain.UsersDomain
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.AddUserToGroupResult
import isel.rl.core.services.interfaces.CreateGroupResult
import isel.rl.core.services.interfaces.DeleteGroupResult
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
        owner: User,
    ): CreateGroupResult =
        runCatching {
            val group = groupsDomain.validateCreateGroup(groupName, groupDescription, clock.now(), owner.id)

            transactionManager.run {
                val groupId = it.groupsRepository.createGroup(group)
                success(group.copy(id = groupId))
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun getGroupById(groupId: String): GetGroupByIdResult =
        runCatching {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)

            transactionManager.run {
                val group =
                    it.groupsRepository.getGroupById(validatedGroupId)
                        ?: return@run failure(groupsDomain.groupNotFound)

                // Get group users
                val usersIds = it.groupsRepository.getGroupUsers(validatedGroupId)
                val usersList =
                    usersIds.map { userId ->
                        it.usersRepository.getUserById(userId)!!
                    }

                success(
                    Group(
                        id = group.id,
                        groupName = group.groupName,
                        groupDescription = group.groupDescription,
                        createdAt = group.createdAt,
                        ownerId = group.ownerId,
                        groupUsers = usersList,
                    ),
                )
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun getUserGroups(
        userId: String,
        limit: String?,
        skip: String?,
    ): GetUserGroupsResult =
        runCatching {
            // Validate limit and skip
            val limitAndSkip = verifyQuery(limit, skip)

            val validatedUserId = usersDomain.validateUserId(userId)

            transactionManager.run {
                if (!it.usersRepository.checkIfUserExists(validatedUserId)) {
                    failure(usersDomain.userNotFound)
                } else {
                    success(it.groupsRepository.getUserGroups(validatedUserId, limitAndSkip))
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun addUserToGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): AddUserToGroupResult =
        runCatching {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)
            val validatedUserId =
                userId?.let(usersDomain::validateUserId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("UserId cannot be null"))

            transactionManager.run {
                val groupRepo = it.groupsRepository
                val userRepo = it.usersRepository

                when {
                    !userRepo.checkIfUserExists(validatedUserId) -> failure(usersDomain.userNotFound)
                    !groupRepo.checkIfGroupExists(validatedGroupId) ||
                        groupRepo.getGroupOwnerId(validatedGroupId) != actorUserId -> failure(groupsDomain.groupNotFound)

                    groupRepo.getGroupUsers(validatedGroupId)
                        .contains(validatedUserId) -> failure(groupsDomain.userAlreadyInGroup)

                    groupRepo.addUserToGroup(validatedUserId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun removeUserFromGroup(
        actorUserId: Int,
        userId: String?,
        groupId: String,
    ): RemoveUserFromGroupResult =
        runCatching {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)
            val validatedUserId =
                userId?.let(usersDomain::validateUserId)
                    ?: return failure(ServicesExceptions.InvalidQueryParam("UserId cannot be null"))

            transactionManager.run {
                val groupRepo = it.groupsRepository
                val userRepo = it.usersRepository

                when {
                    !userRepo.checkIfUserExists(validatedUserId) -> failure(usersDomain.userNotFound)
                    !groupRepo.checkIfGroupExists(validatedGroupId) ||
                        groupRepo.getGroupOwnerId(validatedGroupId) != actorUserId -> failure(groupsDomain.groupNotFound)

                    !groupRepo.getGroupUsers(validatedGroupId)
                        .contains(validatedUserId) -> failure(groupsDomain.userNotInGroup)

                    groupRepo.removeUserFromGroup(validatedUserId, validatedGroupId) -> success(Unit)
                    else -> failure(ServicesExceptions.UnexpectedError)
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }

    override fun deleteGroup(
        actorUserId: Int,
        groupId: String,
    ): DeleteGroupResult =
        runCatching {
            val validatedGroupId = groupsDomain.validateGroupId(groupId)

            transactionManager.run {
                val groupRepo = it.groupsRepository

                if (!groupRepo.checkIfGroupExists(validatedGroupId) || groupRepo.getGroupOwnerId(validatedGroupId) != actorUserId) {
                    failure(groupsDomain.groupNotFound)
                } else {
                    // Delete group users
                    groupRepo.getGroupUsers(validatedGroupId).forEach { userId ->
                        groupRepo.removeUserFromGroup(userId, validatedGroupId)
                    }
                    if (groupRepo.deleteGroup(validatedGroupId)) {
                        success(Unit)
                    } else {
                        failure(ServicesExceptions.UnexpectedError)
                    }
                }
            }
        }.getOrElse { e ->
            handleException(e as Exception)
        }
}
