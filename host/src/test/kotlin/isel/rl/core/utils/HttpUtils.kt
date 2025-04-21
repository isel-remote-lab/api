package isel.rl.core.utils

import isel.rl.core.domain.group.GroupDescription
import isel.rl.core.domain.group.GroupName
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.domain.laboratory.props.LabDescription
import isel.rl.core.domain.laboratory.props.LabName
import isel.rl.core.domain.laboratory.LabSessionState
import isel.rl.core.domain.user.props.Email
import isel.rl.core.domain.user.props.OAuthId
import isel.rl.core.domain.user.props.Role
import isel.rl.core.domain.user.props.Username
import isel.rl.core.host.RemoteLabApp
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class HttpUtils {
    val apiKey = RemoteLabApp().apiKeyInfo()

    fun baseUrl(port: Int) = "http://localhost:$port"

    // User functions
    fun newTestUsername() = Username("user-${abs(Random.nextLong())}")
    fun newTestEmail() = Email("email-${abs(Random.nextLong())}")
    fun randomUserRole() = Role.entries.random()
    fun newTestOauthId() = OAuthId("oauth-${abs(Random.nextLong())}")

    // Group functions
    fun newTestGroupName() = GroupName("group-${abs(Random.nextLong())}")
    fun newTestGroupDescription() = GroupDescription("description-${abs(Random.nextLong())}")

    // Lab functions
    fun newTestLabName() = LabName("lab-${abs(Random.nextLong())}")
    fun newTestLabDescription() = LabDescription("description-${abs(Random.nextLong())}")
    fun newTestLabDuration() = abs(Random.nextInt()).toDuration(DurationUnit.MINUTES)
    fun randomLabQueueLimit() = (1..50).random()
    fun randomLabSessionState() = LabSessionState.entries.random()

    // Hardware functions
    fun newTestHardwareName() = HardwareName("hardware-${abs(Random.nextLong())}")
    fun newTestHardwareSerialNumber() = "serial-${abs(Random.nextLong())}"
    fun randomHardwareStatus() = HardwareStatus.entries.random()
    fun newTestHardwareMacAddress() = "mac-${abs(Random.nextLong())}"
    fun newTestHardwareIpAddress() = "ip-${abs(Random.nextLong())}"

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}