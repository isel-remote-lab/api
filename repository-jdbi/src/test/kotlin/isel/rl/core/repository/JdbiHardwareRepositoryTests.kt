package isel.rl.core.repository

import isel.rl.core.repository.utils.RepoUtils
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.repository.jdbi.JdbiHardwareRepository
import isel.rl.core.repository.utils.TestClock
import kotlin.test.*

class JdbiHardwareRepositoryTests {
    @Test
    fun `store hardware and retrieve`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val macAddress = repoUtils.newTestHardwareMacAddress()
            val ipAddress = repoUtils.newTestHardwareIpAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = macAddress,
                ipAddress = ipAddress,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertEquals(macAddress, hardwareById.macAddress)
            assertEquals(ipAddress, hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: retrieving hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(1, hardwareByName.size)

            // then: delete hardware
            val deleted = hardwareRepo.deleteHardware(hardwareId)
            assertEquals(true, deleted)

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
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = null,
                ipAddress = null,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertNull(hardwareById.macAddress)
            assertNull(hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: retrieving hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(1, hardwareByName.size)

            // then: delete hardware
            val deleted = hardwareRepo.deleteHardware(hardwareId)
            assertEquals(true, deleted)

            // when: trying to retrieve deleted hardware
            val deletedHardware = hardwareRepo.getHardwareById(hardwareId)
            assertNull(deletedHardware, "Hardware was not deleted")
        }
    }

    @Test
    fun `store hardware and update name`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val macAddress = repoUtils.newTestHardwareMacAddress()
            val ipAddress = repoUtils.newTestHardwareIpAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = macAddress,
                ipAddress = ipAddress,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertEquals(macAddress, hardwareById.macAddress)
            assertEquals(ipAddress, hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: updating the name
            val newName = repoUtils.newTestHardwareName()
            val updated = hardwareRepo.updateHardwareName(hardwareId, newName)
            assertEquals(true, updated)

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newName, updatedHardwareById.hwName)

            // when: retrieving updated hardware by name
            val updatedHardwareByName = hardwareRepo.getHardwareByName(newName)
            assertNotNull(updatedHardwareByName) { "No updated hardware retrieved from database" }
        }
    }

    @Test
    fun `store hardware and update status`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = HardwareStatus.Occupied
            val macAddress = repoUtils.newTestHardwareMacAddress()
            val ipAddress = repoUtils.newTestHardwareIpAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = macAddress,
                ipAddress = ipAddress,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertEquals(macAddress, hardwareById.macAddress)
            assertEquals(ipAddress, hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: updating the status
            val newStatus = HardwareStatus.Available
            val updated = hardwareRepo.updateHardwareStatus(hardwareId, newStatus)
            assertEquals(true, updated)

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newStatus, updatedHardwareById.status)

            // when: retrieving updated hardware by name
            val updatedHardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(updatedHardwareByName) { "No updated hardware retrieved from database" }
        }
    }

    @Test
    fun `store hardware with only mac_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val macAddress = repoUtils.newTestHardwareMacAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = macAddress,
                ipAddress = null,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertEquals(macAddress, hardwareById.macAddress)
            assertNull(hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: retrieving hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
        }
    }

    @Test
    fun `store hardware with only ip_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val ipAddress = repoUtils.newTestHardwareIpAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = null,
                ipAddress = ipAddress,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertNull(hardwareById.macAddress)
            assertEquals(ipAddress, hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: retrieving hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
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
            val serialNum1 = repoUtils.newTestHardwareSerialNumber()
            val serialNum2 = repoUtils.newTestHardwareSerialNumber()
            val status1 = HardwareStatus.Occupied
            val status2 = HardwareStatus.Available
            val macAddress1 = repoUtils.newTestHardwareMacAddress()
            val macAddress2 = repoUtils.newTestHardwareMacAddress()
            val ipAddress1 = repoUtils.newTestHardwareIpAddress()
            val ipAddress2 = repoUtils.newTestHardwareIpAddress()
            val createdAt1 = clock.now()
            val createdAt2 = clock.now()

            val hardwareId1 = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum1,
                status = status1,
                macAddress = macAddress1,
                ipAddress = ipAddress1,
                createdAt = createdAt1
            )

            val hardwareId2 = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum2,
                status = status2,
                macAddress = macAddress2,
                ipAddress = ipAddress2,
                createdAt = createdAt2
            )

            // then: retrieve both hardware by name
            val hardwareByName = hardwareRepo.getHardwareByName(hardwareName)
            assertNotNull(hardwareByName) { "No hardware retrieved from database" }
            assertEquals(2, hardwareByName.size)
            assertEquals(hardwareId1, hardwareByName[0].id)
            assertEquals(hardwareId2, hardwareByName[1].id)
            assertEquals(hardwareName, hardwareByName[0].hwName)
            assertEquals(hardwareName, hardwareByName[1].hwName)
            assertEquals(serialNum1, hardwareByName[0].hwSerialNum)
            assertEquals(serialNum2, hardwareByName[1].hwSerialNum)
            assertEquals(status1, hardwareByName[0].status)
            assertEquals(status2, hardwareByName[1].status)
            assertEquals(macAddress1, hardwareByName[0].macAddress)
            assertEquals(macAddress2, hardwareByName[1].macAddress)
            assertEquals(ipAddress1, hardwareByName[0].ipAddress)
            assertEquals(ipAddress2, hardwareByName[1].ipAddress)
            assertEquals(createdAt1, hardwareByName[0].createdAt)
            assertEquals(createdAt2, hardwareByName[1].createdAt)

            // when: deleting both hardware
            val deleted1 = hardwareRepo.deleteHardware(hardwareId1)
            assertTrue(deleted1)
            val deleted2 = hardwareRepo.deleteHardware(hardwareId2)
            assertTrue(deleted2)
        }
    }

    @Test
    fun `update hardware ip_address and mac_address`() {
        repoUtils.runWithHandle { handle ->
            // given: a hardware repo and a clock
            val hardwareRepo = JdbiHardwareRepository(handle)
            val clock = TestClock()

            // when: storing a hardware
            val hardwareName = repoUtils.newTestHardwareName()
            val serialNum = repoUtils.newTestHardwareSerialNumber()
            val status = repoUtils.randomHardwareStatus()
            val macAddress = repoUtils.newTestHardwareMacAddress()
            val ipAddress = repoUtils.newTestHardwareIpAddress()
            val createdAt = clock.now()
            val hardwareId = hardwareRepo.createHardware(
                name = hardwareName,
                serialNum = serialNum,
                status = status,
                macAddress = macAddress,
                ipAddress = ipAddress,
                createdAt = createdAt
            )

            // then: retrieve hardware by Id
            val hardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(hardwareById) { "No hardware retrieved from database" }
            assertEquals(hardwareId, hardwareById.id)
            assertEquals(hardwareName, hardwareById.hwName)
            assertEquals(serialNum, hardwareById.hwSerialNum)
            assertEquals(status, hardwareById.status)
            assertEquals(macAddress, hardwareById.macAddress)
            assertEquals(ipAddress, hardwareById.ipAddress)
            assertEquals(createdAt, hardwareById.createdAt)

            // when: updating the ip_address and mac_address
            val newMacAddress = repoUtils.newTestHardwareMacAddress()
            val newIpAddress = repoUtils.newTestHardwareIpAddress()
            val updatedMac = hardwareRepo.updateHardwareMacAddress(hardwareId, newMacAddress)
            assertTrue(updatedMac)
            val updatedIp = hardwareRepo.updateHardwareIpAddress(hardwareId, newIpAddress)
            assertTrue(updatedIp)

            // then: retrieve updated hardware by Id
            val updatedHardwareById = hardwareRepo.getHardwareById(hardwareId)
            assertNotNull(updatedHardwareById) { "No updated hardware retrieved from database" }
            assertEquals(newMacAddress, updatedHardwareById.macAddress)
            assertEquals(newIpAddress, updatedHardwareById.ipAddress)
        }
    }

    companion object {
        val repoUtils = RepoUtils()
    }
}