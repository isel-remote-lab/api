package isel.rl.core.repository.jdbi

import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.props.HardwareName
import isel.rl.core.domain.hardware.props.HardwareStatus
import isel.rl.core.domain.hardware.props.IpAddress
import isel.rl.core.domain.hardware.props.MacAddress
import isel.rl.core.repository.HardwareRepository
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

data class JdbiHardwareRepository(
    val handle: Handle,
) : HardwareRepository {
    override fun createHardware(
        validatedCreateHardware: Hardware
    ): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.hardware (name, serial_number, status, mac_address, ip_address, created_at)
            VALUES (:name, :serial_number, :status, :mac_address, :ip_address, :created_at)
        """,
        )
            .bind("name", validatedCreateHardware.name.hardwareNameInfo)
            .bind("serial_number", validatedCreateHardware.serialNumber.serialNumberInfo)
            .bind("status", validatedCreateHardware.status.char)
            .bind("mac_address", validatedCreateHardware.macAddress?.address)
            .bind("ip_address", validatedCreateHardware.ipAddress?.address)
            .bind("created_at", validatedCreateHardware.createdAt.toJavaInstant())
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getHardwareById(hwId: Int): Hardware? =
        handle.createQuery(
            """
            SELECT * FROM rl.hardware 
            WHERE id = :id
        """,
        )
            .bind("id", hwId)
            .mapTo<Hardware>()
            .singleOrNull()

    override fun getHardwareByName(hwName: HardwareName): List<Hardware> =
        handle.createQuery(
            """
            SELECT * FROM rl.hardware 
            WHERE name = :name
        """,
        )
            .bind("name", hwName.hardwareNameInfo)
            .mapTo<Hardware>()
            .list()

    override fun updateHardware(
        hwId: Int,
        hwName: HardwareName?,
        hwStatus: HardwareStatus?,
        macAddress: MacAddress?,
        ipAddress: IpAddress?,
    ): Boolean {
        val updateQuery =
            StringBuilder(
                """
            UPDATE rl.hardware 
            SET 
        """,
            )
        val params = mutableMapOf<String, Any?>()

        hwName?.let {
            updateQuery.append("name = :name, ")
            params["name"] = it.hardwareNameInfo
        }
        hwStatus?.let {
            updateQuery.append("status = :status, ")
            params["status"] = it.char
        }
        macAddress?.let {
            updateQuery.append("mac_address = :mac_address, ")
            params["mac_address"] = it.address
        }
        ipAddress?.let {
            updateQuery.append("ip_address = :ip_address, ")
            params["ip_address"] = it.address
        }

        // Remove the last comma and space
        if (params.isNotEmpty()) {
            updateQuery.setLength(updateQuery.length - 2)
        }

        updateQuery.append(" WHERE id = :id")
        params["id"] = hwId

        return handle.createUpdate(updateQuery.toString())
            .bindMap(params)
            .execute() == 1
    }

    override fun deleteHardware(hwId: Int): Boolean =
        handle.createUpdate(
            """
            DELETE FROM rl.hardware 
            WHERE id = :id
        """,
        )
            .bind("id", hwId)
            .execute() == 1 // Check if 1 deletion was performed
}
