package rl.domain.laboratory

enum class LabSessionState(val char: Char) {
    InProgress('P'),
    Completed('C'),
    Scheduled('S'),
}