package isel.rl.core.host

import isel.rl.core.host.utils.HttpUtilsTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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

    @Test
    fun `create group`() {
        // given: a test client
        val testClient = HttpUtilsTest.buildTestClient(port)

        val user = HttpUtilsTest.Users.createTestUser(testClient)

        groupsHelper.createGroup(
            testClient,
            HttpUtilsTest.Groups.InitialGroup(),
            user.authToken,
        )
    }

    companion object {
        private val groupsHelper = HttpUtilsTest.Groups
    }
}
