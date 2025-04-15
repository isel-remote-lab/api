package isel.rl.core.domain.laboratory

enum class LabSessionState(val char: String) {
    InProgress("P"),
    Completed("C"),
    Scheduled("S"),
}
