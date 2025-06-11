package isel.rl.core.repository.jdbi

import isel.rl.core.domain.hardware.Hardware
import isel.rl.core.domain.hardware.HardwareName
import isel.rl.core.domain.hardware.HardwareStatus
import isel.rl.core.repository.HardwareRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

data class JdbiHardwareRepository(
    val handle: Handle,
) : HardwareRepository {
    override fun createHardware(
        name: HardwareName,
        serialNum: String,
        status: HardwareStatus,
        macAddress: String?,
        ipAddress: String?,
        createdAt: Instant,
    ): Int =
        handle.createUpdate(
            """
            INSERT INTO rl.hardware (name, serial_num, status, mac_address, ip_address, created_at)
            VALUES (:name, :serial_num, :status, :mac_address, :ip_address, :created_at)
        """,
        )
            .bind("name", name.hardwareNameInfo)
            .bind("serial_num", serialNum)
            .bind("status", status.char)
            .bind("mac_address", macAddress)
            .bind("ip_address", ipAddress)
            .bind("created_at", createdAt.toJavaInstant())
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
        ipAddress: String?,
        macAddress: String?,
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
        ipAddress?.let {
            updateQuery.append("ip_address = :ip_address, ")
            params["ip_address"] = it
        }
        macAddress?.let {
            updateQuery.append("mac_address = :mac_address, ")
            params["mac_address"] = it
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
