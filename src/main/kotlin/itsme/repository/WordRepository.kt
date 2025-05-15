package itsme.repository

import itsme.service.GeminiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

@Repository
class WordRepository(
    @Autowired private val geminiService: GeminiService
) {

    private val gameAnswers = ConcurrentHashMap<String, String>()
    private val generatedWordsHistory = ConcurrentHashMap.newKeySet<String>()

    private val wordList: List<String> by lazy { loadWords() }

    fun getRandomWord(generateFresh: Boolean = false): Pair<String, String> {
        val gameId = generateGameId()
        val word = if (generateFresh) {
            generateFreshWord()
        } else {
            val availableWords = wordList.filter { !generatedWordsHistory.contains(it) }
            val selectedWord = if (availableWords.isNotEmpty()) {
                availableWords.random()
            } else {
                val usedInGames = gameAnswers.values.toSet()
                generatedWordsHistory.clear()
                generatedWordsHistory.addAll(usedInGames)

                val newAvailableWords = wordList.filter { !generatedWordsHistory.contains(it) }
                if (newAvailableWords.isNotEmpty()) newAvailableWords.random() else wordList.random()
            }

            generatedWordsHistory.add(selectedWord)
            selectedWord
        }
        gameAnswers[gameId] = word
        return Pair(gameId, word)
    }

    fun generateFreshWord(): String {
        for (attempt in 1..3) {
            try {
                val word = geminiService.generateWord()

                if (word.isNotEmpty() && word.length == 5) {
                    if (generatedWordsHistory.contains(word)) {
                        continue
                    }

                    generatedWordsHistory.add(word)

                    if (!wordList.contains(word)) {
                        additionalWords.add(word)
                    }
                    return word
                }
            } catch (e: Exception) {
                println("Failed to generate word: ${e.message}")
            }
        }

        val availableWords = wordList.filter { !generatedWordsHistory.contains(it) }

        return if (availableWords.isNotEmpty()) {
            val word = availableWords.random()
            generatedWordsHistory.add(word)
            word
        } else {
            val usedInGames = gameAnswers.values.toSet()
            generatedWordsHistory.clear()
            generatedWordsHistory.addAll(usedInGames)

            val newAvailableWords = wordList.filter { !generatedWordsHistory.contains(it) }
            val word = if (newAvailableWords.isNotEmpty()) newAvailableWords.random() else wordList.random()
            generatedWordsHistory.add(word)
            word
        }
    }

    fun getAnswerForGame(gameId: String): String? {
        return gameAnswers[gameId]
    }

    private val additionalWords = ConcurrentHashMap.newKeySet<String>()

    fun updateGameWord(gameId: String, newWord: String): Boolean {
        if (gameAnswers.containsKey(gameId)) {
            val normalizedWord = newWord.lowercase()

            if (normalizedWord.length == 5 && !wordList.contains(normalizedWord)) {
                additionalWords.add(normalizedWord)
            }

            gameAnswers[gameId] = normalizedWord
            return true
        }
        return false
    }

    fun isValidWord(word: String): Boolean {
        val normalizedWord = word.lowercase()

        if (normalizedWord.length != 5) return false

        if (wordList.contains(normalizedWord) || additionalWords.contains(normalizedWord)) {
            return true
        }

        val isValid = geminiService.isValidEnglishWord(normalizedWord)

        if (isValid) {
            additionalWords.add(normalizedWord)
        }

        return isValid
    }

    private fun generateGameId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    private fun loadWords(): List<String> {
        val defaultWords = listOf(
            "apple", "table", "chair", "plant", "water", "phone", "music",
            "happy", "dance", "beach", "clock", "fancy", "grace", "hands",
            "light", "money", "ocean", "price", "queen", "shiny", "tiger",
            "union", "voice", "winds", "xrays", "youth", "zebra", "about",
            "begin", "cover", "dance", "earth", "flame", "grade", "house"
        )

        val geminiWord = geminiService.generateWord()
        if (geminiWord.isNotEmpty() && geminiWord.length == 5) {
            val wordList = mutableListOf(geminiWord)

            try {
                val resourcePath = "/words.txt"
                val inputStream = javaClass.getResourceAsStream(resourcePath)

                if (inputStream != null) {
                    val fileWords = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.lines()
                            .filter { it.length == 5 }
                            .map { it.trim().lowercase() }
                            .toList()
                    }
                    wordList.addAll(fileWords)
                }
            } catch (e: Exception) {
                println("Failed to load additional words: ${e.message}")
            }

            return wordList
        }

        val resourcePath = "/words.txt"
        return try {
            val inputStream = javaClass.getResourceAsStream(resourcePath)

            if (inputStream != null) {
                val fileWords = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.lines()
                        .filter { it.length == 5 }
                        .map { it.trim().lowercase() }
                        .toList()
                }
                fileWords
            } else {
                defaultWords
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWords
        }
    }
}
