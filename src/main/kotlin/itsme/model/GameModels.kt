package itsme.model

data class GameResponse(
    val gameId: String,
    val message: String,
    val wordLength: Int = 5
)

data class GuessRequest(
    val guess: String
)

data class LetterResult(
    val letter: Char,
    val status: LetterStatus
)

enum class LetterStatus {
    CORRECT,
    PRESENT,
    ABSENT
}

data class GuessResponse(
    val correct: Boolean,
    val message: String,
    val results: List<LetterResult>,
    val attemptNumber: Int,
    val gameOver: Boolean
)
