package itsme.service

import itsme.model.GameStatus
import itsme.model.LetterResult
import itsme.model.LetterStatus
import itsme.repository.WordRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class WordleService(
    private val wordRepository: WordRepository,
    private val geminiService: GeminiService
) {

    private val gameAttempts = ConcurrentHashMap<String, Int>()
    private val maxAttempts = 6

    fun startNewGame(): Pair<String, String> {
        val (gameId, _) = wordRepository.getRandomWord(generateFresh = true)
        gameAttempts[gameId] = 0
        return Pair(gameId, "New game started! You have $maxAttempts attempts to guess a 5-letter word.")
    }

    fun processGuess(gameId: String, guess: String): Pair<List<LetterResult>, GameStatus> {
        val answer = wordRepository.getAnswerForGame(gameId)

        if (answer == null) {
            return Pair(emptyList(), GameStatus(false, "Game not found", 0, true))
        }

        if (guess.length != 5) {
            return Pair(emptyList(), GameStatus(false, "Guess must be 5 letters", gameAttempts[gameId] ?: 0, false))
        }

        if (!wordRepository.isValidWord(guess)) {
            return Pair(emptyList(), GameStatus(false, "Not a valid word", gameAttempts[gameId] ?: 0, false))
        }

        val attemptNumber = (gameAttempts[gameId] ?: 0) + 1
        gameAttempts[gameId] = attemptNumber

        val guessLowercase = guess.lowercase()
        val answerLowercase = answer.lowercase()

        val isCorrect = guessLowercase == answerLowercase
        val isGameOver = isCorrect || attemptNumber >= maxAttempts

        val results = checkGuess(guessLowercase, answerLowercase)

        val newWord = if (isCorrect) {
            var generatedWord = geminiService.generateWord()

            var attempts = 0
            while (!wordRepository.isValidWord(generatedWord) && attempts < 2) {
                generatedWord = geminiService.generateWord()
                attempts++
            }

            if (!wordRepository.isValidWord(generatedWord)) {
                val fallbackWords = listOf("table", "chair", "space", "light", "music", "plant")
                generatedWord = fallbackWords.random()
            }

            wordRepository.updateGameWord(gameId, generatedWord)
            gameAttempts[gameId] = 0
            generatedWord
        } else null

        val message = when {
            isCorrect -> "Congratulations! You've guessed the word correctly! A new word has been generated for you."
            isGameOver -> "Game over! The word was: $answer"
            else -> "Try again. ${maxAttempts - attemptNumber} attempts remaining."
        }

        return Pair(results, GameStatus(isCorrect, message, attemptNumber, isGameOver, newWord))
    }

    private fun checkGuess(guess: String, answer: String): List<LetterResult> {
        val results = mutableListOf<LetterResult>()
        val answerCharCount = answer.groupingBy { it }.eachCount().toMutableMap()

        val firstPassResults = Array(5) { index ->
            val guessChar = guess[index]
            val answerChar = answer[index]

            if (guessChar == answerChar) {
                answerCharCount[guessChar] = answerCharCount[guessChar]!! - 1
                LetterResult(guessChar, LetterStatus.CORRECT)
            } else {
                null
            }
        }

        for (index in guess.indices) {
            if (firstPassResults[index] != null) {
                results.add(firstPassResults[index]!!)
                continue
            }

            val guessChar = guess[index]
            val remaining = answerCharCount[guessChar] ?: 0

            if (remaining > 0) {
                answerCharCount[guessChar] = remaining - 1
                results.add(LetterResult(guessChar, LetterStatus.PRESENT))
            } else {
                results.add(LetterResult(guessChar, LetterStatus.ABSENT))
            }
        }

        return results
    }
}
