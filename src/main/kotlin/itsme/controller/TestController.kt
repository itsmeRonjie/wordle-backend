package itsme.controller

import itsme.service.GeminiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(private val geminiService: GeminiService) {

    @GetMapping("/gemini-word")
    fun testGeminiWordGeneration(): ResponseEntity<Map<String, String>> {
        return try {
            val generatedWord = geminiService.generateWord()
            ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "generatedWord" to generatedWord
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "status" to "error",
                    "message" to "Failed to generate word: ${e.message}",
                    "error" to e.javaClass.simpleName
                )
            )
        }
    }
}
