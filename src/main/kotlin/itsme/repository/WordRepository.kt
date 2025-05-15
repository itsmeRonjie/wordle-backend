package itsme.repository

import org.springframework.stereotype.Repository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

@Repository
class WordRepository {
    
    private val wordList: List<String> = loadWords()

    private val gameAnswers = ConcurrentHashMap<String, String>()
    
    fun getRandomWord(): Pair<String, String> {
        val gameId = generateGameId()
        val word = wordList.random()
        gameAnswers[gameId] = word
        return Pair(gameId, word)
    }
    
    fun getAnswerForGame(gameId: String): String? {
        return gameAnswers[gameId]
    }
    
    fun isValidWord(word: String): Boolean {
        return word.length == 5 && wordList.contains(word.lowercase())
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

        val resourcePath = "/words.txt"
        return try {
            val inputStream = javaClass.getResourceAsStream(resourcePath)
            
            if (inputStream != null) {
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.lines()
                        .filter { it.length == 5 }
                        .map { it.trim().lowercase() }
                        .toList()
                }
            } else {
                defaultWords
            }
        } catch (e: Exception) {
            defaultWords
        }
    }
}
