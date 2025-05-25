package isel.rl.core.host

import isel.rl.core.domain.Uris
import isel.rl.core.domain.user.User.Companion.ROLE_PROP
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.Name
import isel.rl.core.domain.user.props.Role
import isel.rl.core.host.utils.HttpUtils
import isel.rl.core.host.utils.UsersTestsUtils
import isel.rl.core.http.model.Problem
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [RemoteLabApp::class],
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class UsersTests {
    // This is the port that will be used to run the tests
    // Property is injected by Spring
    @LocalServerPort
    var port: Int = 0

    @Nested
    inner class CreateOrLoginUser {
        @Test
        fun `login user test`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: logging a user that does not exist
            val user = UsersTestsUtils.createTestUser(testClient)

            // then: the user is created
            UsersTestsUtils.getUserById(testClient, user)
        }

        @Test
        fun `login already created user`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: logging a user that does not exist
            val user = UsersTestsUtils.createTestUser(testClient)

            // then: the user is created
            UsersTestsUtils.getUserById(testClient, user)

            // when: logging the same user again
            val userSecondLogin = UsersTestsUtils.createTestUser(testClient, user)

            // then: the user is the same
            assertEquals(user.id, userSecondLogin.id)
        }

        @Test
        fun `create user with invalid email`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            val initialUser =
                UsersTestsUtils.InitialUser(
                    email = Email(""),
                )

            // when: doing a POST
            UsersTestsUtils.createTestUser(testClient, initialUser, Problem.invalidEmail)
        }

        @Test
        fun `create user with invalid username`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            val initialUser =
                UsersTestsUtils.InitialUser(
                    name = Name(""),
                )

            // when: doing a POST
            UsersTestsUtils.createTestUser(testClient, initialUser, Problem.invalidName)
        }

        @Test
        fun `create user without api key`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            val initialUser =
                UsersTestsUtils.InitialUser()

            // when: doing a POST
            // then: the response is an 403 Forbidden
            testClient
                .post()
                .uri(Uris.Auth.LOGIN)
                .bodyValue(
                    UsersTestsUtils.InitialUser.createBodyValue(initialUser),
                )
                .exchange()
                .expectStatus().isForbidden
        }

        @Test
        fun `create user with invalid api key`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            val initialUser = UsersTestsUtils.InitialUser()

            // when: doing a POST
            // then: the response is an 403 Forbidden
            testClient
                .post()
                .uri(Uris.Auth.LOGIN)
                .header(HttpUtils.API_HEADER_NAME, "invalid-api-key")
                .bodyValue(
                    UsersTestsUtils.InitialUser.createBodyValue(initialUser),
                )
                .exchange()
                .expectStatus().isForbidden
        }
    }

    @Nested
    inner class UserRetrieval {
        @Test
        fun `Get by Id`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: doing a GET by id
            UsersTestsUtils.getUserById(testClient, user)

            // when: doing a GET by email
            UsersTestsUtils.getUserByEmail(testClient, user)
        }

        @Test
        fun `Get by Email`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating a user
            val user = UsersTestsUtils.createTestUser(testClient)

            // when: doing a GET by email
            UsersTestsUtils.getUserByEmail(testClient, user)
        }
    }

    @Nested
    inner class UpdateUser {
        @Test
        fun `update user role`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating the actor user directly in the DB to be Admin
            val actorUserInitialInfo = UsersTestsUtils.InitialUser()

            UsersTestsUtils.createDBUser(
                actorUserInitialInfo.name.nameInfo,
                actorUserInitialInfo.email.emailInfo,
                Role.ADMIN.char,
            )

            // when: logging the actor user
            val actorUser = UsersTestsUtils.createTestUser(testClient, actorUserInitialInfo)

            // when: creating the target user
            val targetUser = UsersTestsUtils.createTestUser(testClient)

            // when: updating the user role
            val newRole = UsersTestsUtils.randomUserRole()
            testClient
                .patch()
                .uri(Uris.Users.UPDATE_USER_ROLE, targetUser.id)
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${actorUser.authToken}")
                .bodyValue(
                    mapOf(
                        ROLE_PROP to newRole.char,
                    ),
                )
                .exchange()
                .expectStatus().isOk

            // then: the user role is updated
            UsersTestsUtils.getUserById(testClient, targetUser.copy(role = newRole))
        }

        @Test
        fun `update user role (not enough permission)`() {
            // given: a test client
            val testClient = HttpUtils.buildTestClient(port)

            // when: creating the actor user directly in the DB to be User
            val actorUserInitialInfo = UsersTestsUtils.InitialUser()

            UsersTestsUtils.createDBUser(
                actorUserInitialInfo.name.nameInfo,
                actorUserInitialInfo.email.emailInfo,
                Role.STUDENT.char,
            )

            // when: logging the actor user
            val actorUser = UsersTestsUtils.createTestUser(testClient, actorUserInitialInfo)

            // when: creating the target user
            val targetUser = UsersTestsUtils.createTestUser(testClient)

            // when: updating the user role
            val newRole = UsersTestsUtils.randomUserRole()
            testClient
                .patch()
                .uri(Uris.Users.UPDATE_USER_ROLE, targetUser.id)
                .header(HttpUtils.AUTH_HEADER_NAME, "Bearer ${actorUser.authToken}")
                .bodyValue(
                    mapOf(
                        ROLE_PROP to newRole.char,
                    ),
                )
                .exchange()
                .expectStatus().isForbidden
        }
    }
}
