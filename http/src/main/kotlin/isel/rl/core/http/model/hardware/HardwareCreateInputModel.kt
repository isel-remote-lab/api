package isel.rl.core.http.model.hardware

data class HardwareCreateInputModel(
    val name: String,
    val serialNumber: String,
    val status: String? = null,
    val macAddress: String? = null,
    val ipAddress: String? = null,
)
