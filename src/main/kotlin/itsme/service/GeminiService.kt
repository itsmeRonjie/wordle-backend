package itsme.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.logging.Logger

@Service
class GeminiService(@Value("\${gemini.api.key}") private val apiKey: String) {

    private val webClient = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
        .build()

    private val logger = Logger.getLogger(GeminiService::class.java.name)

    fun generateWord(): String {
        try {
            logger.info("Generating a fresh word from Gemini API...")
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = "Generate a single 5-letter English word suitable for a Wordle game." +
                                        " Respond with just the word and nothing else." +
                                        " The word should be common enough that most people would know it."
                            )
                        )
                    )
                )
            )

            val response = webClient.post()
                .uri("/gemini-2.0-flash:generateContent?key=${apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .block()

            val generatedText = response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val cleanedWord = generatedText.trim().replace("[^a-zA-Z]".toRegex(), "").lowercase()
            val validWord = if (cleanedWord.length == 5) cleanedWord else "nymph"
            logger.info("Generated a fresh word: $validWord")
            return validWord
        } catch (e: Exception) {
            logger.warning("Error generating word from Gemini API: ${e.message}")
            return "nymph"
        }
    }

    data class GeminiRequest(val contents: List<Content>)

    data class Content(val parts: List<Part>)

    data class Part(val text: String)

    data class GeminiResponse(val candidates: List<Candidate>?)

    data class Candidate(val content: Content)

    fun isValidEnglishWord(word: String): Boolean {
        try {
            val cleanWord = word.trim().lowercase()

            if (cleanWord.length != 5) return false

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = "Is \"$cleanWord\" a valid 5-letter English word that could be used in a Wordle game? " +
                                        "Respond with just 'yes' or 'no' and nothing else."
                            )
                        )
                    )
                )
            )

            val response = webClient.post()
                .uri("/gemini-2.0-flash:generateContent?key=${apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .block()

            val answer =
                response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()?.lowercase() ?: ""

            return answer.contains("yes")
        } catch (e: Exception) {
            logger.warning("Error checking word validity with Gemini API: ${e.message}")
            return false
        }
    }
}
