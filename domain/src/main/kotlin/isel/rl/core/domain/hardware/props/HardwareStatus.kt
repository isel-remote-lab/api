package isel.rl.core.domain.hardware.props

enum class HardwareStatus(val char: String?) {
    Available("A"),
    Occupied("O"),
    Maintenance("M"),
    ;

    companion object {
        fun from(char: String): HardwareStatus =
            when (char) {
                "A" -> Available
                "O" -> Occupied
                "M" -> Maintenance
                else -> throw IllegalStateException("Character $char is not supported")
            }
    }
}
