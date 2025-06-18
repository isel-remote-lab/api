package isel.rl.core.domain.hardware.props

enum class HardwareStatus(val char: String) {
    Available("A"),
    Occupied("O"),
    Maintenance("M"),
}
