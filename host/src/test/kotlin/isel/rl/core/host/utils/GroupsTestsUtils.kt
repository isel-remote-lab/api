package isel.rl.core.host.utils

import isel.rl.core.domain.Uris
import isel.rl.core.domain.group.props.GroupDescription
import isel.rl.core.domain.group.props.GroupName
import isel.rl.core.host.utils.HttpUtils.AUTH_HEADER_NAME
import isel.rl.core.host.utils.HttpUtils.assertProblem
import isel.rl.core.host.utils.HttpUtils.domainConfigs
import isel.rl.core.host.utils.HttpUtils.getBodyDataFromResponse
import isel.rl.core.http.model.Problem
import isel.rl.core.http.model.SuccessResponse
import kotlinx.datetime.Instant
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days

object GroupsTestsUtils {
    private const val ID_PROP = "id"
    private const val GROUP_NAME_PROP = "groupName"
    private const val GROUP_DESCRIPTION_PROP = "groupDescription"
    private const val CREATED_AT_PROP = "createdAt"
    private const val OWNER_ID_PROP = "ownerId"
    private const val USERS_PROP = "users"

    data class InitialGroup(
        val id: Int = 0,
        val name: GroupName = newTestGroupName(),
        val description: GroupDescription = newTestGroupDescription(),
        val createdAt: Instant = Instant.DISTANT_PAST,
        val ownerId: Int = 0,
    ) {
        companion object {
            fun createBodyValue(initialGroup: InitialGroup) =
                mapOf(
                    GROUP_NAME_PROP to initialGroup.name.groupNameInfo,
                    GROUP_DESCRIPTION_PROP to initialGroup.description.groupDescriptionInfo,
                )
        }
    }

    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")

    fun newTestInvalidGroupNameMax() = GroupName("a".repeat(domainConfigs.group.groupName.max + 1))

    fun newTestInvalidGroupNameMin() = GroupName("a")

    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    fun newTestInvalidGroupDescriptionMax() = GroupDescription("a".repeat(domainConfigs.group.groupDescription.max + 1))

    fun newTestInvalidGroupDescriptionMin() = GroupDescription("a")

    fun createGroup(
        testClient: WebTestClient,
        initialGroup: InitialGroup = InitialGroup(),
        authToken: String,
        expectedProblem: Problem? = null,
        expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    ): InitialGroup {
        if (expectedProblem != null) {
            testClient.post()
                .uri(Uris.Groups.CREATE)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .bodyValue(InitialGroup.createBodyValue(initialGroup))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody<Problem>()
                .consumeWith { assertProblem(expectedProblem, it) }
            return initialGroup
        }

        val response =
            testClient.post()
                .uri(Uris.Groups.CREATE)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .bodyValue(InitialGroup.createBodyValue(initialGroup))
                .exchange()
                .expectStatus().isCreated
                .expectBody<SuccessResponse>()
                .returnResult()

        val group = getBodyDataFromResponse<Map<*, *>>(response, "Group created successfully")

        assertEquals(initialGroup.name.groupNameInfo, group[GROUP_NAME_PROP], "Group name mismatch")
        assertEquals(
            initialGroup.description.groupDescriptionInfo,
            group[GROUP_DESCRIPTION_PROP],
            "Group description mismatch",
        )

        return initialGroup.copy(
            id = group[ID_PROP] as Int,
            createdAt = Instant.parse(group[CREATED_AT_PROP] as String),
            ownerId = group[OWNER_ID_PROP] as Int,
        )
    }

    fun getGroupById(
        testClient: WebTestClient,
        authToken: String,
        expectedGroup: InitialGroup,
        expectedUsersList: List<UsersTestsUtils.InitialUser>,
    ) {
        val response =
            testClient.get()
                .uri(Uris.Groups.GET_BY_ID, expectedGroup.id)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        val group = getBodyDataFromResponse<Map<*, *>>(response, "Group retrieved successfully")
        assertGroup(expectedGroup, group)

        assertEquals(
            expectedUsersList.size,
            (group[USERS_PROP] as List<*>).size,
            "Group users list size mismatch",
        )

        (group[USERS_PROP] as List<*>).forEach { user ->
            val userMap = user as Map<*, *>
            val expectedUser = expectedUsersList.find { it.id == userMap[ID_PROP] }
            assertNotNull(expectedUser)
            UsersTestsUtils.assertUser(expectedUser, userMap)
        }
    }

    fun getGroupById(
        testClient: WebTestClient,
        authToken: String,
        groupId: String,
        expectedProblem: Problem,
        expectedStatus: HttpStatus = HttpStatus.NOT_FOUND,
    ) {
        testClient.get()
            .uri(Uris.Groups.GET_BY_ID, groupId)
            .header(AUTH_HEADER_NAME, "Bearer $authToken")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody<Problem>()
            .consumeWith { assertProblem(expectedProblem, it) }
    }

    fun getUserGroups(
        testClient: WebTestClient,
        authToken: String,
        targetUserId: Int = -1,
        expectedGroups: List<InitialGroup>,
    ) {
        val response =
            testClient.get()
                .uri { builder ->
                    val path = builder.path(Uris.Groups.GET_USER_GROUPS)
                    if (targetUserId != -1) {
                        path.queryParam("userId", targetUserId)
                    }
                    path.build()
                }
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        val groups = getBodyDataFromResponse<List<*>>(response, "Groups retrieved successfully")
        assertEquals(
            expectedGroups.size,
            groups.size,
            "User groups list size mismatch",
        )
        groups.forEach { group ->
            val groupMap = group as Map<*, *>
            val expectedGroup = expectedGroups.find { it.id == groupMap[ID_PROP] }
            assertNotNull(expectedGroup)
            assertGroup(expectedGroup, groupMap)
        }
    }

    fun getUserGroups(
        testClient: WebTestClient,
        authToken: String,
        targetUserId: Int = -1,
        expectedProblem: Problem,
        expectedStatus: HttpStatus = HttpStatus.NOT_FOUND,
    ) {
        testClient.get()
            .uri { builder ->
                val path = builder.path(Uris.Groups.GET_USER_GROUPS)
                if (targetUserId != -1) {
                    path.queryParam("userId", targetUserId)
                }
                path.build()
            }
            .header(AUTH_HEADER_NAME, "Bearer $authToken")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody<Problem>()
            .consumeWith { assertProblem(expectedProblem, it) }
    }

    fun addUserToGroup(
        testClient: WebTestClient,
        authToken: String,
        groupId: Int,
        userId: Int,
    ) {
        val response =
            testClient.patch()
                .uri { builder ->
                    builder
                        .path(Uris.Groups.ADD_USER_TO_GROUP)
                        .queryParam("userId", userId)
                        .build(groupId.toString())
                }
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        getBodyDataFromResponse<String?>(response, "User added to group successfully", true)
    }

    fun addUserToGroup(
        testClient: WebTestClient,
        authToken: String,
        groupId: String,
        userId: String?,
        expectedProblem: Problem,
        expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    ) {
        testClient.patch()
            .uri { builder ->
                val path =
                    builder
                        .path(Uris.Groups.ADD_USER_TO_GROUP)

                if (userId != null) {
                    path.queryParam("userId", userId)
                }

                path.build(groupId)
            }
            .header(AUTH_HEADER_NAME, "Bearer $authToken")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody<Problem>()
            .consumeWith { assertProblem(expectedProblem, it) }
    }

    fun removeUserFromGroup(
        testClient: WebTestClient,
        authToken: String,
        groupId: Int,
        userId: Int,
    ) {
        val response =
            testClient.delete()
                .uri { builder ->
                    builder
                        .path(Uris.Groups.REMOVE_USER_FROM_GROUP)
                        .queryParam("userId", userId)
                        .build(groupId.toString())
                }
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isOk
                .expectBody<SuccessResponse>()
                .returnResult()

        getBodyDataFromResponse<String?>(response, "User removed from group successfully", true)
    }

    fun removeUserFromGroup(
        testClient: WebTestClient,
        authToken: String,
        groupId: String,
        userId: String?,
        expectedProblem: Problem,
        expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    ) {
        testClient.delete()
            .uri { builder ->
                val path =
                    builder
                        .path(Uris.Groups.REMOVE_USER_FROM_GROUP)

                if (userId != null) {
                    path.queryParam("userId", userId)
                }

                path.build(groupId)
            }
            .header(AUTH_HEADER_NAME, "Bearer $authToken")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody<Problem>()
            .consumeWith { assertProblem(expectedProblem, it) }
    }

    fun deleteGroup(
        testClient: WebTestClient,
        authToken: String,
        groupId: String,
        expectedProblem: Problem? = null,
        expectedStatus: HttpStatus = HttpStatus.OK,
    ) {
        if (expectedProblem != null) {
            testClient.delete()
                .uri(Uris.Groups.DELETE, groupId)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody<Problem>()
                .consumeWith { assertProblem(expectedProblem, it) }
            return
        }

        val response =
            testClient.delete()
                .uri(Uris.Groups.DELETE, groupId)
                .header(AUTH_HEADER_NAME, "Bearer $authToken")
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody<SuccessResponse>()
                .returnResult()

        getBodyDataFromResponse<String?>(response, "Group deleted successfully", true)
    }

    val expectedRequiredGroupNameProblem =
        Problem.invalidGroupName(HttpUtils.groupsDomain.groupNameRequiredMsg)

    val expectedInvalidGroupNameLengthProblem =
        Problem.invalidGroupName(HttpUtils.groupsDomain.invalidGroupNameLengthMsg)

    val expectedRequiredGroupDescriptionProblem =
        Problem.invalidGroupDescription(HttpUtils.groupsDomain.groupDescriptionRequiredMsg)

    val expectedInvalidGroupDescriptionLengthProblem =
        Problem.invalidGroupDescription(HttpUtils.groupsDomain.invalidGroupDescriptionLengthMsg)

    private const val INVALID_USER_ID_QUERY_PARAM =
        "UserId cannot be null"

    val expectedInvalidUserIdQueryParamProblem =
        Problem.invalidQueryParam(
            INVALID_USER_ID_QUERY_PARAM,
        )

    fun assertGroup(
        expectedGroup: InitialGroup,
        group: Map<*, *>,
    ) {
        assertEquals(expectedGroup.id, group[ID_PROP], "Group ID mismatch")
        assertEquals(expectedGroup.name.groupNameInfo, group[GROUP_NAME_PROP], "Group name mismatch")
        assertEquals(
            expectedGroup.description.groupDescriptionInfo,
            group[GROUP_DESCRIPTION_PROP],
            "Group description mismatch",
        )
        assertEquals(
            expectedGroup.createdAt.epochSeconds.days,
            Instant.parse(group[CREATED_AT_PROP] as String).epochSeconds.days,
            "Group createdAt mismatch",
        )
        assertEquals(expectedGroup.ownerId, group[OWNER_ID_PROP], "Group ownerId mismatch")
    }
}
