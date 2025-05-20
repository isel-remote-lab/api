package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.Failure
import isel.rl.core.utils.Success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupsServicesTests {
    @Test
    fun `create group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group should be created successfully
        assertTrue(group is Success, "Group creation failed. Expected a success result but got $group")
    }

    @Test
    fun `create group with invalid group name (min)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group with an invalid name
        val groupName = servicesUtils.newTestInvalidGroupNameMin()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group creation should fail
        assertTrue(group is Failure, "Group creation should have failed. Expected a failure result but got $group")
        assertTrue(
            group.value is ServicesExceptions.Groups.InvalidGroupName,
            "Expected an IllegalArgumentException but got ${group.value}",
        )
    }

    @Test
    fun `create group with invalid group name (max)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group with an invalid name
        val groupName = servicesUtils.newTestInvalidGroupNameMax()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group creation should fail
        assertTrue(group is Failure, "Group creation should have failed. Expected a failure result but got $group")
        assertTrue(
            group.value is ServicesExceptions.Groups.InvalidGroupName,
            "Expected an IllegalArgumentException but got ${group.value}",
        )
    }

    @Test
    fun `create group with invalid group description (min)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group with an invalid description
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestInvalidGroupDescriptionMin()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group creation should fail
        if (servicesUtils.isGroupDescriptionOptional) {
            assertTrue(
                group is Success,
                "Group creation should have succeeded. Expected a success result but got $group",
            )
        } else {
            // Then: the group creation should fail
            assertTrue(group is Failure, "Group creation should have failed. Expected a failure result but got $group")
            assertTrue(
                group.value is ServicesExceptions.Groups.InvalidGroupDescription,
                "Expected an InvalidGroupDescription but got ${group.value}",
            )
        }
    }

    @Test
    fun `create group with invalid group description (max)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group with an invalid description
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestInvalidGroupDescriptionMax()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group creation should fail
        assertTrue(group is Failure, "Group creation should have failed. Expected a failure result but got $group")
        assertTrue(
            group.value is ServicesExceptions.Groups.InvalidGroupDescription,
            "Expected an InvalidGroupDescription but got ${group.value}",
        )
    }

    @Test
    fun `get group by id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = TEST_USER_ID,
            )

        // Then: the group should be created successfully
        assertTrue(group is Success, "Group creation failed. Expected a success result but got $group")

        // When: Getting the group by id
        val getGroupResult = groupService.getGroupById(group.value.id.toString())

        // Then: the group should be retrieved successfully
        assertTrue(
            getGroupResult is Success,
            "Group retrieval failed. Expected a success result but got $getGroupResult",
        )

        assertEquals(group.value, getGroupResult.value.group)
        assertTrue(
            getGroupResult.value.users.size == 1,
            "Expected 1 user in the group but got ${getGroupResult.value.users.size}",
        )
    }

    @Test
    fun `get group by id with invalid id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Getting the group by id with an invalid id
        val getGroupResult = groupService.getGroupById("invalid_id")

        // Then: the group retrieval should fail
        assertTrue(
            getGroupResult is Failure,
            "Group retrieval should have failed. Expected a failure result but got $getGroupResult",
        )
        assertTrue(
            getGroupResult.value is ServicesExceptions.Groups.InvalidGroupId,
            "Expected an InvalidGroupId but got ${getGroupResult.value}",
        )
    }

    @Test
    fun `get non existent group by id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Getting a non-existent group by id
        val getGroupResult = groupService.getGroupById("999")

        // Then: the group retrieval should fail
        assertTrue(
            getGroupResult is Failure,
            "Group retrieval should have failed. Expected a failure result but got $getGroupResult",
        )
        assertTrue(
            getGroupResult.value is ServicesExceptions.Groups.GroupNotFound,
            "Expected a GroupNotFound but got ${getGroupResult.value}",
        )
    }

    @Test
    fun `get user groups`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = user.id,
            )

        val group2 =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = user.id,
            )

        // Then: the groups should be created successfully
        assertTrue(group is Success, "Group creation failed. Expected a success result but got $group")
        assertTrue(group2 is Success, "Group creation failed. Expected a success result but got $group2")

        // When: Getting the user groups
        val getUserGroupsResult = groupService.getUserGroups(user.id.toString())

        // Then: the user groups should be retrieved successfully
        assertTrue(
            getUserGroupsResult is Success,
            "User groups retrieval failed. Expected a success result but got $getUserGroupsResult",
        )
        assertTrue(
            getUserGroupsResult.value.size == 2,
            "Expected 2 groups but got ${getUserGroupsResult.value.size}",
        )
    }

    @Test
    fun `get user groups with invalid id (not a number)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Getting the user groups with an invalid id
        val getUserGroupsResult = groupService.getUserGroups("invalid_id")

        // Then: the user groups retrieval should fail
        assertTrue(
            getUserGroupsResult is Failure,
            "User groups retrieval should have failed. Expected a failure result but got $getUserGroupsResult",
        )
        assertTrue(
            getUserGroupsResult.value is ServicesExceptions.Users.InvalidUserId,
            "Expected an InvalidUserId but got ${getUserGroupsResult.value}",
        )
    }

    @Test
    fun `get user groups with non existent id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Getting the user groups with a non-existent id
        val getUserGroupsResult = groupService.getUserGroups("999")

        // Then: the user groups retrieval should fail
        assertTrue(
            getUserGroupsResult is Failure,
            "User groups retrieval should have failed. Expected a failure result but got $getUserGroupsResult",
        )
        assertTrue(
            getUserGroupsResult.value is ServicesExceptions.Users.UserNotFound,
            "Expected a UserNotFound exception but got ${getUserGroupsResult.value}",
        )
    }

    @Test
    fun `add user to group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user and a user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            groupService.createGroup(
                groupName = groupName,
                groupDescription = groupDescription,
                ownerId = owner.id,
            )

        // Then: the group should be created successfully
        assertTrue(group is Success, "Group creation failed. Expected a success result but got $group")

        // When: Adding a user to the group
        val addUserResult = groupService.addUserToGroup(owner.id, user.id.toString(), group.value.id.toString())

        // Then: the user should be added to the group successfully
        assertTrue(addUserResult is Success, "User addition failed. Expected a success result but got $addUserResult")

        // When: Getting the group by id and checking the users
        val getGroupResult = (groupService.getGroupById(group.value.id.toString()) as Success).value
        assertTrue(getGroupResult.users.size == 2, "Expected 2 users in the group but got ${getGroupResult.users.size}")
        assertTrue(
            getGroupResult.users.any { it.id == user.id },
            "Expected the user to be in the group but it was not found",
        )
        assertTrue(
            getGroupResult.users.any { it.id == owner.id },
            "Expected the owner to be in the group but it was not found",
        )
    }

    @Test
    fun `add user to group with invalid user id (not a number)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Adding a user to the group with an invalid user id
        val addUserResult = groupService.addUserToGroup(1, "invalid_id", "1")

        // Then: the user addition should fail
        assertTrue(
            addUserResult is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult",
        )
        assertTrue(
            addUserResult.value is ServicesExceptions.Users.InvalidUserId,
            "Expected an InvalidUserId exception but got ${addUserResult.value}",
        )
    }

    @Test
    fun `add user to group with invalid user id (null)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Adding a user to the group with an invalid user id
        val addUserResult = groupService.addUserToGroup(1, null, "1")

        // Then: the user addition should fail
        assertTrue(
            addUserResult is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult",
        )
        assertTrue(
            addUserResult.value is ServicesExceptions.InvalidQueryParam,
            "Expected an InvalidQueryParam but got ${addUserResult.value}",
        )
    }

    @Test
    fun `add user to group with invalid group id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Adding a user to the group with an invalid group id
        val addUserResult = groupService.addUserToGroup(1, "1", "invalid_id")

        // Then: the user addition should fail
        assertTrue(
            addUserResult is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult",
        )
        assertTrue(
            addUserResult.value is ServicesExceptions.Groups.InvalidGroupId,
            "Expected an InvalidGroupId but got ${addUserResult.value}",
        )
    }

    @Test
    fun `add a non existing user to group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user and a user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a non-existing user to the group
        val addUserResult = groupService.addUserToGroup(owner.id, "999", group.id.toString())

        // Then: the user addition should fail
        assertTrue(
            addUserResult is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult",
        )
        assertTrue(
            addUserResult.value is ServicesExceptions.Users.UserNotFound,
            "Expected a UserNotFound but got ${addUserResult.value}",
        )
    }

    @Test
    fun `add user to a non existing group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Adding a user to a non-existing group
        val addUserResult = groupService.addUserToGroup(1, user.id.toString(), "999")

        // Then: the user addition should fail
        assertTrue(
            addUserResult is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult",
        )
        assertTrue(
            addUserResult.value is ServicesExceptions.Groups.GroupNotFound,
            "Expected a GroupNotFound but got ${addUserResult.value}",
        )
    }

    @Test
    fun `add a user to a group (user already in)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a user to the group
        groupService.addUserToGroup(owner.id, user.id.toString(), group.id.toString())

        // When: Adding the same user to the same group again
        val addUserResult2 = groupService.addUserToGroup(owner.id, user.id.toString(), group.id.toString())

        // Then: the user addition should fail
        assertTrue(
            addUserResult2 is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult2",
        )
        assertTrue(
            addUserResult2.value is ServicesExceptions.Groups.UserAlreadyInGroup,
            "Expected a UserAlreadyInGroup but got ${addUserResult2.value}",
        )
    }

    @Test
    fun `add a user to a group (actor user is not the owner)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a user to the group with an invalid actor id
        val addUserResult2 = groupService.addUserToGroup(user.id, user.id.toString(), group.id.toString())

        // Then: the user addition should fail
        assertTrue(
            addUserResult2 is Failure,
            "User addition should have failed. Expected a failure result but got $addUserResult2",
        )
        assertTrue(
            addUserResult2.value is ServicesExceptions.Groups.GroupNotFound,
            "Expected a GroupNotFound but got ${addUserResult2.value}",
        )
    }

    @Test
    fun `remove user from group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user and a user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a user to the group
        groupService.addUserToGroup(owner.id, user.id.toString(), group.id.toString())

        // When: Removing the user from the group
        val removeUserResult = groupService.removeUserFromGroup(owner.id, user.id.toString(), group.id.toString())

        // Then: the user should be removed from the group successfully
        assertTrue(
            removeUserResult is Success,
            "User removal failed. Expected a success result but got $removeUserResult",
        )

        // When: Getting the group by id and checking the users
        val getGroupResult = (groupService.getGroupById(group.id.toString()) as Success).value
        assertTrue(getGroupResult.users.size == 1, "Expected 1 user in the group but got ${getGroupResult.users.size}")
    }

    @Test
    fun `remove user from group with invalid user id (not a number)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Removing a user from the group with an invalid user id
        val removeUserResult = groupService.removeUserFromGroup(1, "invalid_id", "1")

        // Then: the user removal should fail
        assertTrue(
            removeUserResult is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult",
        )
        assertTrue(
            removeUserResult.value is ServicesExceptions.Users.InvalidUserId,
            "Expected an InvalidUserId exception but got ${removeUserResult.value}",
        )
    }

    @Test
    fun `remove user from group with invalid user id (null)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Removing a user from the group with an invalid user id
        val removeUserResult = groupService.removeUserFromGroup(1, null, "1")

        // Then: the user removal should fail
        assertTrue(
            removeUserResult is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult",
        )
        assertTrue(
            removeUserResult.value is ServicesExceptions.InvalidQueryParam,
            "Expected an InvalidQueryParam but got ${removeUserResult.value}",
        )
    }

    @Test
    fun `remove user from group with invalid group id`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)

        // When: Removing a user from the group with an invalid group id
        val removeUserResult = groupService.removeUserFromGroup(1, "1", "invalid_id")

        // Then: the user removal should fail
        assertTrue(
            removeUserResult is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult",
        )
        assertTrue(
            removeUserResult.value is ServicesExceptions.Groups.InvalidGroupId,
            "Expected an InvalidGroupId but got ${removeUserResult.value}",
        )
    }

    @Test
    fun `remove a non existing user from group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user and a user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Removing a non-existent user from the group
        val removeUserResult = groupService.removeUserFromGroup(owner.id, "999", group.id.toString())

        // Then: the user removal should fail
        assertTrue(
            removeUserResult is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult",
        )
        assertTrue(
            removeUserResult.value is ServicesExceptions.Users.UserNotFound,
            "Expected a UserNotFound but got ${removeUserResult.value}",
        )
    }

    @Test
    fun `remove already removed user from group`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a user to the group
        groupService.addUserToGroup(owner.id, user.id.toString(), group.id.toString())

        // When: Removing the user from the group
        groupService.removeUserFromGroup(owner.id, user.id.toString(), group.id.toString())

        // When: Removing the same user from the same group again
        val removeUserResult2 = groupService.removeUserFromGroup(owner.id, user.id.toString(), group.id.toString())

        // Then: the user removal should fail
        assertTrue(
            removeUserResult2 is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult2",
        )
        assertTrue(
            removeUserResult2.value is ServicesExceptions.Groups.UserNotInGroup,
            "Expected a UserNotInGroup but got ${removeUserResult2.value}",
        )
    }

    @Test
    fun `remove user from group (actor user is not the owner)`() {
        // When: given a group service
        val clock = TestClock()
        val groupService = servicesUtils.createGroupsServices(clock)
        val userService = servicesUtils.createUsersServices(clock)

        // When: Creating a owner user
        val owner =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a user
        val user =
            (
                userService.createUser(
                    role = servicesUtils.randomUserRole(),
                    name = servicesUtils.newTestUsername(),
                    email = servicesUtils.newTestEmail(),
                ) as Success
            ).value

        // When: Creating a group
        val groupName = servicesUtils.newTestGroupName()
        val groupDescription = servicesUtils.newTestGroupDescription()

        val group =
            (
                groupService.createGroup(
                    groupName = groupName,
                    groupDescription = groupDescription,
                    ownerId = owner.id,
                ) as Success
            ).value

        // When: Adding a user to the group
        groupService.addUserToGroup(owner.id, user.id.toString(), group.id.toString())

        // When: Removing a user from the group with an invalid actor id
        val removeUserResult2 = groupService.removeUserFromGroup(user.id, user.id.toString(), group.id.toString())

        // Then: the user removal should fail
        assertTrue(
            removeUserResult2 is Failure,
            "User removal should have failed. Expected a failure result but got $removeUserResult2",
        )
        assertTrue(
            removeUserResult2.value is ServicesExceptions.Groups.GroupNotFound,
            "Expected a GroupNotFound but got ${removeUserResult2.value}",
        )
    }

    companion object {
        val servicesUtils = ServicesUtils()
        private const val TEST_USER_ID = 1
    }
}
