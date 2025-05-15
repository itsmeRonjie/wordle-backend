package itsme.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun index(): ResponseEntity<Map<String, Any>> {
        val response = mapOf(
            "application" to "Wordle Game API",
            "endpoints" to listOf(
                mapOf(
                    "path" to "/api/wordle/start",
                    "method" to "GET",
                    "description" to "Start a new game and get a game ID"
                ),
                mapOf(
                    "path" to "/api/wordle/{gameId}/guess",
                    "method" to "POST",
                    "requestBody" to mapOf("guess" to "5-letter word"),
                    "description" to "Submit a guess for the current game"
                ),
                mapOf(
                    "path" to "/api/test/gemini-word",
                    "method" to "GET",
                    "description" to "Test the Gemini API word generation"
                )
            ),
            "status" to "running"
        )
        return ResponseEntity.ok(response)
    }
}
