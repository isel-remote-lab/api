package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.domain.user.props.Role
import isel.rl.core.services.utils.GroupsServicesUtils
import isel.rl.core.services.utils.UsersServicesUtils
import isel.rl.core.utils.Success
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class GroupsServicesTests {
    @Nested
    inner class GroupCreation {
        @Test
        fun `create group (user is Teacher)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user to be the owner of the group
            val owner =
                (
                    usersService.createUser(
                        role = Role.TEACHER.char,
                        name = UsersServicesUtils.newTestUsername(),
                        email = UsersServicesUtils.newTestEmail(),
                    ) as Success
                ).value

            // When: Creating a group
            GroupsServicesUtils.createGroup(
                groupService,
                owner = owner,
            )
        }

        @Test
        fun `create group (user is Admin)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val usersService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user to be the owner of the group
            val owner =
                (
                    usersService.createUser(
                        role = Role.ADMIN.char,
                        name = UsersServicesUtils.newTestUsername(),
                        email = UsersServicesUtils.newTestEmail(),
                    ) as Success
                ).value

            // When: Creating a group
            GroupsServicesUtils.createGroup(
                groupService,
                owner = owner,
            )
        }

        @Test
        fun `create group with invalid group name (min)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group with an invalid name
            GroupsServicesUtils.createGroup(
                groupService,
                initialGroup =
                    GroupsServicesUtils.InitialGroup(
                        name = GroupsServicesUtils.newTestInvalidGroupNameMin(),
                    ),
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupName::class,
            )
        }

        @Test
        fun `create group with invalid group name (max)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group with an invalid name
            GroupsServicesUtils.createGroup(
                groupService,
                initialGroup =
                    GroupsServicesUtils.InitialGroup(
                        name = GroupsServicesUtils.newTestInvalidGroupNameMax(),
                    ),
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupName::class,
            )
        }

        @Test
        fun `create group with invalid group description (min)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group with an invalid description
            if (GroupsServicesUtils.isGroupDescriptionOptional) {
                GroupsServicesUtils.createGroup(
                    groupService,
                    initialGroup =
                        GroupsServicesUtils.InitialGroup(
                            description = GroupsServicesUtils.newTestInvalidGroupDescriptionMin(),
                        ),
                )
            } else {
                GroupsServicesUtils.createGroup(
                    groupService,
                    initialGroup =
                        GroupsServicesUtils.InitialGroup(
                            description = GroupsServicesUtils.newTestInvalidGroupDescriptionMin(),
                        ),
                    expectedServiceException = ServicesExceptions.Groups.InvalidGroupDescription::class,
                )
            }
        }

        @Test
        fun `create group with invalid group description (max)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group with an invalid description
            GroupsServicesUtils.createGroup(
                groupService,
                initialGroup =
                    GroupsServicesUtils.InitialGroup(
                        description = GroupsServicesUtils.newTestInvalidGroupDescriptionMax(),
                    ),
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupDescription::class,
            )
        }
    }

    @Nested
    inner class GroupRetrieval {
        @Test
        fun `get group by id`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Getting the group by id
            GroupsServicesUtils.getGroupById(
                groupService,
                expectedGroup = group,
            )
        }

        @Test
        fun `get group by id with invalid id`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the group by id with an invalid id
            GroupsServicesUtils.getGroupById(
                groupService,
                "invalid_id",
                ServicesExceptions.Groups.InvalidGroupId::class,
            )
        }

        @Test
        fun `get non existent group by id`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting a non-existent group by id
            GroupsServicesUtils.getGroupById(
                groupService,
                "999",
                ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `get group by id with invalid id (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the group by id with an invalid id (negative)
            GroupsServicesUtils.getGroupById(
                groupService,
                "-1",
                ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `get user groups`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user to be the owner of the group
            val owner =
                (
                    userService.createUser(
                        role = Role.TEACHER.char,
                        name = UsersServicesUtils.newTestUsername(),
                        email = UsersServicesUtils.newTestEmail(),
                    ) as Success
                ).value

            // When: Creating a group for the user
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                    owner = owner,
                )

            // When: Getting the user groups
            GroupsServicesUtils.getUserGroups(
                groupService,
                owner.id.toString(),
                expectedGroups = listOf(group),
            )
        }

        @Test
        fun `get user groups with invalid id (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid id
            GroupsServicesUtils.getUserGroups(
                groupService,
                "invalid_id",
                expectedServiceException = ServicesExceptions.Users.InvalidUserId::class,
            )
        }

        @Test
        fun `get user groups with non existent id`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with a non-existent id
            GroupsServicesUtils.getUserGroups(
                groupService,
                "999",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `get user groups with invalid id (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid id (negative)
            GroupsServicesUtils.getUserGroups(
                groupService,
                "-1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `get user groups with limit and skip`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a user to be the owner of the groups
            val owner =
                (
                    userService.createUser(
                        role = Role.TEACHER.char,
                        name = UsersServicesUtils.newTestUsername(),
                        email = UsersServicesUtils.newTestEmail(),
                    ) as Success
                ).value

            // When: Creating multiple groups
            val groups =
                List(3) {
                    GroupsServicesUtils.createGroup(
                        groupService,
                        owner = owner,
                    )
                }

            // When: Getting the user groups with limit and skip
            GroupsServicesUtils.getUserGroups(
                groupService,
                owner.id.toString(),
                limit = "2",
                skip = "1",
                expectedGroups = listOf(groups[1], groups[2]),
            )
        }

        @Test
        fun `get user groups with invalid limit (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid limit (negative)
            GroupsServicesUtils.getUserGroups(
                groupService,
                "1",
                limit = "-1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user groups with invalid limit (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid limit (not a number)
            GroupsServicesUtils.getUserGroups(
                groupService,
                "1",
                limit = "invalid_limit",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user groups with invalid skip (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid skip (negative)
            GroupsServicesUtils.getUserGroups(
                groupService,
                "1",
                skip = "-1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get user groups with invalid skip (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Getting the user groups with an invalid skip (not a number)
            GroupsServicesUtils.getUserGroups(
                groupService,
                "1",
                skip = "invalid_skip",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `get group users`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user to be added to the group
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Getting the users of the group
            GroupsServicesUtils.getGroupUsers(
                groupService,
                group.id.toString(),
                expectedUsers = listOf(group.ownerId, user.id),
            )
        }
    }

    @Nested
    inner class AddUserToGroup {
        @Test
        fun `add user to group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Getting the group by id and checking the users
            GroupsServicesUtils.getGroupById(
                groupService,
                group,
            )
        }

        @Test
        fun `add user to group with invalid group id (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "1",
                groupId = "-1",
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `add user to group with invalid group id (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "1",
                groupId = "invalid_id",
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupId::class,
            )
        }

        @Test
        fun `add user to group with invalid user id (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Adding a user to the group with an invalid user id
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "-1",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `add user to group with invalid user id (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Adding a user to the group with an invalid user id
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "invalid_id",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.InvalidUserId::class,
            )
        }

        @Test
        fun `add user to group with invalid user id (null)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Adding a user to the group with an invalid user id
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = null,
                groupId = "1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `add a non existing user to group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Adding a non-existing user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "999",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `add a user to a non existent group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Adding a user to a non-existing group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = 1,
                userId = "1",
                groupId = "999",
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `add user to a group with an actor that is not the owner`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group with an invalid actor id
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = user.id,
                userId = user.id.toString(),
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `add user to a group (user already in)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Adding the same user to the same group again
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.UserAlreadyInGroup::class,
            )
        }
    }

    @Nested
    inner class RemoveUserFromGroup {
        @Test
        fun `remove user from group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Removing the user from the group
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Getting the group by id and checking the users
            GroupsServicesUtils.getGroupById(
                groupService,
                group,
            )
        }

        @Test
        fun `remove user from group with invalid group id (negative)`() {
            // When: given a group service}
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a user from the group with an invalid group id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = "1",
                groupId = "-1",
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `remove user from group with invalid group id (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a user from the group with an invalid group id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = "1",
                groupId = "invalid_id",
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupId::class,
            )
        }

        @Test
        fun `remove user from group with invalid user id (negative)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a user from the group with an invalid user id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = "-1",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `remove user from group with invalid user id (not a number)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a user from the group with an invalid user id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = "invalid_id",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.InvalidUserId::class,
            )
        }

        @Test
        fun `remove user from group with invalid user id (null)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a user from the group with an invalid user id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = null,
                groupId = "1",
                expectedServiceException = ServicesExceptions.InvalidQueryParam::class,
            )
        }

        @Test
        fun `remove a non existing user from group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Removing a non-existent user from the group
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = 1,
                userId = "999",
                groupId = "1",
                expectedServiceException = ServicesExceptions.Users.UserNotFound::class,
            )
        }

        @Test
        fun `remove user from group (actor user is not the owner)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Removing the user from the group with an invalid actor id
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = user.id,
                userId = user.id.toString(),
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `remove already removed user from group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Adding the user to the group
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Removing the user from the group
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Removing the same user from the same group again
            GroupsServicesUtils.removeUserFromGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.UserNotInGroup::class,
            )
        }
    }

    @Nested
    inner class DeleteGroup {
        @Test
        fun `delete group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Deleting the group
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = group.ownerId,
                groupId = group.id.toString(),
            )

            // When: Getting the group by id and checking if it exists
            GroupsServicesUtils.getGroupById(
                groupService,
                group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `delete group with users`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Adding a user to the group
            val user = UsersServicesUtils.loginUser(userService)
            GroupsServicesUtils.addUserToGroup(
                groupService,
                actorUserId = group.ownerId,
                userId = user.id.toString(),
                groupId = group.id.toString(),
            )

            // When: Deleting the group with users
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = group.ownerId,
                groupId = group.id.toString(),
            )

            // When: Getting the group by id and checking if it exists
            GroupsServicesUtils.getGroupById(
                groupService,
                group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `delete group with invalid group id`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Deleting a group with an invalid group id
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = 1,
                groupId = "invalid_id",
                expectedServiceException = ServicesExceptions.Groups.InvalidGroupId::class,
            )
        }

        @Test
        fun `delete non existing group`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Deleting a non-existent group
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = 1,
                groupId = "999",
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `delete already deleted group`() {
            // When: given a group service and user service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Deleting the group
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = group.ownerId,
                groupId = group.id.toString(),
            )

            // When: Deleting the same group again
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = group.ownerId,
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }

        @Test
        fun `delete group (actor user is not the owner)`() {
            // When: given a group service
            val clock = TestClock()
            val groupService = GroupsServicesUtils.createGroupsServices(clock)
            val userService = UsersServicesUtils.createUsersServices(clock)

            // When: Creating a group
            val group =
                GroupsServicesUtils.createGroup(
                    groupService,
                )

            // When: Creating a user
            val user = UsersServicesUtils.loginUser(userService)

            // When: Deleting the group with an invalid actor id
            GroupsServicesUtils.deleteGroup(
                groupService,
                actorUserId = user.id,
                groupId = group.id.toString(),
                expectedServiceException = ServicesExceptions.Groups.GroupNotFound::class,
            )
        }
    }
}
