package rl.domain.hardware

enum class HardwareStatus(val char: String) {
    Available("A"),
    Occupied("O"),
    Maintenance("M")
}