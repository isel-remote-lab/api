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
            INSERT INTO rl.hardware (hw_name, hw_serial_num, status, mac_address, ip_address, created_at)
            VALUES (:hw_name, :hw_serial_num, :status, :mac_address, :ip_address, :created_at)
        """,
        )
            .bind("hw_name", name.hardwareNameInfo)
            .bind("hw_serial_num", serialNum)
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
            WHERE hw_name = :hw_name
        """,
        )
            .bind("hw_name", hwName.hardwareNameInfo)
            .mapTo<Hardware>()
            .list()

    override fun updateHardwareName(
        hwId: Int,
        hwName: HardwareName,
    ): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.hardware 
            SET hw_name = :hw_name
            WHERE id = :id
        """,
        )
            .bind("hw_name", hwName.hardwareNameInfo)
            .bind("id", hwId)
            .execute() == 1

    override fun updateHardwareStatus(
        hwId: Int,
        hwStatus: HardwareStatus,
    ): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.hardware 
            SET status = :status
            WHERE id = :id
        """,
        )
            .bind("status", hwStatus.char)
            .bind("id", hwId)
            .execute() == 1

    override fun updateHardwareIpAddress(
        hwId: Int,
        ipAddress: String,
    ): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.hardware 
            SET ip_address = :ip_address
            WHERE id = :id
        """,
        )
            .bind("ip_address", ipAddress)
            .bind("id", hwId)
            .execute() == 1

    override fun updateHardwareMacAddress(
        hwId: Int,
        macAddress: String,
    ): Boolean =
        handle.createUpdate(
            """
            UPDATE rl.hardware 
            SET mac_address = :mac_address
            WHERE id = :id
        """,
        )
            .bind("mac_address", macAddress)
            .bind("id", hwId)
            .execute() == 1

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
