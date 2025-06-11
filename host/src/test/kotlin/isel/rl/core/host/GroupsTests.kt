package isel.rl.core.host

import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.utils.GroupsTestsUtils
import isel.rl.core.host.utils.GroupsTestsUtils.expectedRequiredGroupDescriptionProblem
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.UsersTestsUtils
import isel.rl.core.http.model.Problem
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [RemoteLabApp::class],
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class GroupsTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Nested
    inner class GroupCreation {
        @Test
        fun `create group with teacher user`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(),
                user.authToken,
            )
        }

        @Test
        fun `create group with admin user`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating an admin
            val user = UsersTestsUtils.createUser(testClient, Role.ADMIN)

            // then: creating a group
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(),
                user.authToken,
            )
        }

        @Test
        fun `create group with invalid name (blank)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid name (min)
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(name = GroupName()),
                user.authToken,
                GroupsTestsUtils.expectedRequiredGroupNameProblem,
            )
        }

        @Test
        fun `create group with invalid name (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid name (min)
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(name = GroupsTestsUtils.newTestInvalidGroupNameMin()),
                user.authToken,
                GroupsTestsUtils.expectedInvalidGroupNameLengthProblem,
            )
        }

        @Test
        fun `create group with invalid name (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid name (max)
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(name = GroupsTestsUtils.newTestInvalidGroupNameMax()),
                user.authToken,
                GroupsTestsUtils.expectedInvalidGroupNameLengthProblem,
            )
        }

        /**
         * This test has in consideration the group description optionality
         */
        @Test
        fun `create group with blank description`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid description
            if (HttpUtils.domainConfigs.group.description.optional) {
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(description = GroupDescription()),
                    user.authToken,
                )
            } else {
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(description = GroupDescription()),
                    user.authToken,
                    expectedRequiredGroupDescriptionProblem,
                )
            }
        }

        @Test
        fun `create group with invalid description (min)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid description (min)
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(description = GroupsTestsUtils.newTestInvalidGroupDescriptionMin()),
                user.authToken,
                GroupsTestsUtils.expectedInvalidGroupDescriptionLengthProblem,
            )
        }

        @Test
        fun `create group with invalid description (max)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group with invalid description (max)
            GroupsTestsUtils.createGroup(
                testClient,
                GroupsTestsUtils.InitialGroup(description = GroupsTestsUtils.newTestInvalidGroupDescriptionMax()),
                user.authToken,
                GroupsTestsUtils.expectedInvalidGroupDescriptionLengthProblem,
            )
        }
    }

    @Nested
    inner class GroupRetrieval {
        @Test
        fun `get group by id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: getting the group by id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                group,
            )
        }

        @Test
        fun `get group by id with invalid id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // then: getting the group by id with invalid id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                "invalidId",
                Problem.invalidGroupId,
                HttpStatus.BAD_REQUEST,
            )
        }

        @Test
        fun `get non existent group by id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // then: getting the group by id with not found id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                "999999",
                Problem.groupNotFound,
            )
        }

        @Test
        fun `get user groups (authenticated user groups)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: getting the user groups
            GroupsTestsUtils.getUserGroups(
                testClient,
                user.authToken,
                expectedGroups = listOf(group),
            )
        }

        @Test
        fun `get user groups (no existent user)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // then: getting the user groups with not found id
            GroupsTestsUtils.getUserGroups(
                testClient,
                user.authToken,
                expectedProblem = Problem.userNotFound,
                targetUserId = 999999,
            )
        }

        @Test
        fun `get group users`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: getting the group users
            GroupsTestsUtils.getGroupUsers(
                testClient,
                user.authToken,
                group.id,
                expectedUsers = listOf(user.id),
            )
        }
    }

    @Nested
    inner class AddUserToGroup {
        @Test
        fun `add user to group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating a new user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: adding the user to the group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                group.id,
                newUser.id,
            )

            // and: getting the group by id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                group,
            )
        }

        @Test
        fun `add user to group with invalid user id (null)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val groupId = "1" // Does not matter in this test case

            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                groupId,
                null,
                GroupsTestsUtils.expectedInvalidUserIdQueryParamProblem,
            )
        }

        @Test
        fun `add user to group with invalid group id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val userId = "1" // Does not matter in this test case

            // and: adding the user to the group with invalid id
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                "invalid_id",
                userId,
                Problem.invalidGroupId,
            )
        }

        @Test
        fun `add a non existing user to group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val groupId = "1" // Does not matter in this test case

            // and: adding a non existing user to the group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                groupId,
                "999999",
                Problem.userNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `add user to a non existing group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating another user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: adding the user to a non existing group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                "999999",
                newUser.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `add a user to a group (user already in)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating a new user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: adding the user to the group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                group.id,
                newUser.id,
            )

            // and: adding the same user to the group again
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                group.id.toString(),
                newUser.id.toString(),
                Problem.userAlreadyInGroup,
            )
        }

        @Test
        fun `add a user to a group (actor user is not the owner)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating another user (not the owner)
            val anotherUser = UsersTestsUtils.createUser(testClient)

            // and: trying to add the user to the group (not the owner)
            GroupsTestsUtils.addUserToGroup(
                testClient,
                anotherUser.authToken,
                group.id.toString(),
                anotherUser.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }
    }

    @Nested
    inner class RemoveUserFromGroup {
        @Test
        fun `remove user from group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating a new user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: adding the user to the group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                group.id,
                newUser.id,
            )

            // and: removing the user from the group
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                group.id,
                newUser.id,
            )

            // and: getting the group by id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                group,
            )
        }

        @Test
        fun `remove user from group with invalid user id (null)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val groupId = "1" // Does not matter in this test case

            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                groupId,
                null,
                GroupsTestsUtils.expectedInvalidUserIdQueryParamProblem,
            )
        }

        @Test
        fun `remove user from group with invalid group id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val userId = "1" // Does not matter in this test case

            // and: removing the user from the group with invalid id
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                "invalid_id",
                userId,
                Problem.invalidGroupId,
            )
        }

        @Test
        fun `remove a non existing user from group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)
            val groupId = "1" // Does not matter in this test case

            // and: removing a non existing user from the group
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                groupId,
                "999999",
                Problem.userNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `remove user from a non existing group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // when: creating another user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: removing the user from a non existing group
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                "999999",
                newUser.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `remove a user from a group (user not in group)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating a new user
            val newUser = UsersTestsUtils.createTestUser(testClient)

            // and: removing the user from the group (not in group)
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                user.authToken,
                group.id.toString(),
                newUser.id.toString(),
                Problem.userNotInGroup,
            )
        }

        @Test
        fun `remove a user from a group (actor user is not the owner)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating another user (not the owner)
            val anotherUser = UsersTestsUtils.createUser(testClient)

            // and: removing the user from the group (not the owner)
            GroupsTestsUtils.removeUserFromGroup(
                testClient,
                anotherUser.authToken,
                group.id.toString(),
                anotherUser.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }
    }

    @Nested
    inner class DeleteGroup {
        @Test
        fun `delete group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: deleting the group
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                group.id.toString(),
            )

            // and: getting the group by id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                group.id.toString(),
                Problem.groupNotFound,
            )
        }

        @Test
        fun `delete group with invalid group id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // then: deleting the group with invalid id
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                "invalid_id",
                Problem.invalidGroupId,
                HttpStatus.BAD_REQUEST,
            )
        }

        @Test
        fun `delete non existent group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createUser(testClient)

            // then: deleting the non existent group
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                "999999",
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `delete already deleted group`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: deleting the group
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                group.id.toString(),
            )

            // and: deleting the same group again
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                group.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `delete group (actor user is not the owner)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating another user (not the owner)
            val anotherUser = UsersTestsUtils.createUser(testClient)

            // and: deleting the group (not the owner)
            GroupsTestsUtils.deleteGroup(
                testClient,
                anotherUser.authToken,
                group.id.toString(),
                Problem.groupNotFound,
                HttpStatus.NOT_FOUND,
            )
        }

        @Test
        fun `delete group with users`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a teacher
            val user = UsersTestsUtils.createUser(testClient)

            // then: creating a group
            val group =
                GroupsTestsUtils.createGroup(
                    testClient,
                    GroupsTestsUtils.InitialGroup(),
                    user.authToken,
                )

            // and: creating another user
            val anotherUser = UsersTestsUtils.createTestUser(testClient)

            // and: adding the user to the group
            GroupsTestsUtils.addUserToGroup(
                testClient,
                user.authToken,
                group.id,
                anotherUser.id,
            )

            // and: deleting the group
            GroupsTestsUtils.deleteGroup(
                testClient,
                user.authToken,
                group.id.toString(),
            )

            // and: getting the group by id
            GroupsTestsUtils.getGroupById(
                testClient,
                user.authToken,
                group.id.toString(),
                Problem.groupNotFound,
            )
        }
    }
}
