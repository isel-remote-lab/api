package isel.rl.core.repository

import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.repository.jdbi.JdbiHardwareRepository
import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.repository.utils.TestClock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JdbiHardwareRepositoryTests {
    @Test
    fun `store hardware and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // then: retrieve hardware by Id and verify it
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            initialHardware.assertHardwareWith(hardwareById)

            // when: retrieving hardware by name and verify it
            val hardwareByName = hardwareRepo.getHardwareByName(initialHardware.hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(1, hardwareByName.size, "Expected only one hardware with the same name")
            initialHardware.assertHardwareWith(hardwareByName[0])

            // then: delete hardware
            val deleted = hardwareRepo.deleteHardware(hardwareId)
            assertEquals(true, deleted, "Hardware was not deleted")

            // when: trying to retrieve deleted hardware
            val deletedHardware = hardwareRepo.getHardwareById(hardwareId)
            assertNull(deletedHardware, "Hardware was not deleted")
        }
    }

    @Test
    fun `store hardware without Ip address and mac address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware =
                InitialHardware(
                    clock,
                    macAddress = null,
                    ipAddress = null,
                )
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // then: retrieve hardware by Id and verify it
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            initialHardware.assertHardwareWith(hardwareById)

            // when: retrieving hardware by name and verify it
            val hardwareByName = hardwareRepo.getHardwareByName(initialHardware.hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(1, hardwareByName.size, "Expected only one hardware with the same name")
            initialHardware.assertHardwareWith(hardwareByName[0])
        }
    }

    @Test
    fun `store hardware with only mac_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock, ipAddress = null)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            initialHardware.assertHardwareWith(hardwareById)
        }
    }

    @Test
    fun `store hardware with only ip_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock, macAddress = null)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            initialHardware.assertHardwareWith(hardwareById)
        }
    }

    @Test
    fun `store two hardware with same name and retrieve them`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing two hardware with same name
            val hardwareName = repoUtils.newTestHardwareName()
            val initialHardware1 = InitialHardware(clock, hardwareName = hardwareName)
            val initialHardware2 = InitialHardware(clock, hardwareName = hardwareName)
            val hardwareId1 = hardwareRepo.createHardware(initialHardware1)
            val hardwareId2 = hardwareRepo.createHardware(initialHardware2)

            // then: retrieve both hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(2, hardwareByName.size, "Expected two hardware with the same name")
            initialHardware1.assertHardwareWith(hardwareByName[0])
            initialHardware2.assertHardwareWith(hardwareByName[1])

            // when: deleting both hardware
            assertTrue(hardwareRepo.deleteHardware(hardwareId1), "Hardware was not deleted")
            assertTrue(hardwareRepo.deleteHardware(hardwareId2), "Hardware was not deleted")
        }
    }

    @Test
    fun `store hardware and update name`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // when: updating the name
            val newName = repoUtils.newTestHardwareName()
            assertTrue(hardwareRepo.updateHardware(hardwareId, newName), "Hardware name was not updated")

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newName, updatedHardwareById.name, "Hardware names do not match")

            // when: trying to retrieve updated hardware by old name
            assertTrue(
                hardwareRepo.getHardwareByName(initialHardware.hardwareName).isEmpty(),
                "Old name should not retrieve hardware",
            )

            // when: retrieving updated hardware by new name
            assertNotNull(hardwareRepo.getHardwareByName(newName)) {
                "No updated hardware retrieved from database"
            }
        }
    }

    @Test
    fun `store hardware and update status`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // when: updating the status
            // Guarantee to be different from initial status
            val newStatus =
                HardwareStatus.entries
                    .filter { it != initialHardware.status }
                    .random()

            assertTrue(hardwareRepo.updateHardware(hardwareId, hwStatus = newStatus), "Hardware status was not updated")

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newStatus, updatedHardwareById.status, "Hardware statuses do not match")
        }
    }

    @Test
    fun `update hardware ip_address and mac_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // when: updating the ip_address and mac_address
            val newMacAddress = repoUtils.newTestHardwareMacAddress()
            val newIpAddress = repoUtils.newTestHardwareIpAddress()
            assertTrue(
                hardwareRepo.updateHardware(
                    hardwareId,
                    ipAddress = newIpAddress,
                    macAddress = newMacAddress,
                ),
                "Hardware ip_address and mac_address were not updated",
            )

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newMacAddress, updatedHardwareById.macAddress, "Hardware mac addresses do not match")
            assertEquals(newIpAddress, updatedHardwareById.ipAddress, "Hardware ip addresses do not match")
        }
    }

    @Test
    fun `update hardware ip_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // when: updating the ip_address
            val newIpAddress = repoUtils.newTestHardwareIpAddress()
            assertTrue(
                hardwareRepo.updateHardware(
                    hardwareId,
                    ipAddress = newIpAddress,
                    macAddress = null,
                ),
                "Hardware ip_address was not updated",
            )

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newIpAddress, updatedHardwareById.ipAddress, "Hardware ip addresses do not match")
        }
    }

    @Test
    fun `update hardware mac_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val initialHardware = InitialHardware(clock)
            val hardwareId = hardwareRepo.createHardware(initialHardware)

            // when: updating the mac_address
            val newMacAddress = repoUtils.newTestHardwareMacAddress()
            assertTrue(
                hardwareRepo.updateHardware(
                    hardwareId,
                    ipAddress = null,
                    macAddress = newMacAddress,
                ),
                "Hardware mac_address was not updated",
            )

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newMacAddress, updatedHardwareById.macAddress, "Hardware mac addresses do not match")
        }
    }

    companion object {
        val repoUtils = RepoUtils()

        private data class InitialHardware(
            val clock: TestClock,
            val hardwareName: HardwareName = repoUtils.newTestHardwareName(),
            val serialNum: String = repoUtils.newTestHardwareSerialNumber(),
            val status: HardwareStatus = repoUtils.randomHardwareStatus(),
            val macAddress: String? = repoUtils.newTestHardwareMacAddress(),
            val ipAddress: String? = repoUtils.newTestHardwareIpAddress(),
            val createdAt: Instant = clock.now(),
        )

        private fun JdbiHardwareRepository.createHardware(hardware: InitialHardware): Int {
            return createHardware(
                name = hardware.hardwareName,
                serialNum = hardware.serialNum,
                status = hardware.status,
                macAddress = hardware.macAddress,
                ipAddress = hardware.ipAddress,
                createdAt = hardware.createdAt,
            )
        }

        private fun InitialHardware.assertHardwareWith(hardware: Hardware?) {
            assertNotNull(hardware) { "No hardware retrieved" }
            assertEquals(hardwareName, hardware.name, "Hardware names do not match")
            assertEquals(serialNum, hardware.serialNum, "Hardware serial numbers do not match")
            assertEquals(status, hardware.status, "Hardware statuses do not match")
            assertEquals(macAddress, hardware.macAddress, "Hardware mac addresses do not match")
            assertEquals(ipAddress, hardware.ipAddress, "Hardware ip addresses do not match")
            assertEquals(createdAt, hardware.createdAt, "CreatedAt do not match")
            assertTrue(hardware.id >= 0, "HardwareId must be >= 0")
        }
    }
}
