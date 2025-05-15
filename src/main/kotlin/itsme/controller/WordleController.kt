package itsme.controller

import itsme.model.GameResponse
import itsme.model.GuessRequest
import itsme.model.GuessResponse
import itsme.service.WordleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/wordle")
class WordleController(private val wordleService: WordleService) {

    @GetMapping("/start")
    fun startGame(): ResponseEntity<GameResponse> {
        val (gameId, message) = wordleService.startNewGame()
        return ResponseEntity.ok(GameResponse(gameId, message))
    }

    @PostMapping("/{gameId}/guess")
    fun makeGuess(
        @PathVariable gameId: String,
        @RequestBody request: GuessRequest
    ): ResponseEntity<GuessResponse> {
        val (results, status) = wordleService.processGuess(gameId, request.guess)

        val response = GuessResponse(
            correct = status.isCorrect,
            message = status.message,
            results = results,
            attemptNumber = status.attemptNumber,
            gameOver = status.isGameOver
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/admin/answer/{gameId}")
    fun getAnswer(@PathVariable gameId: String): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("message" to "This endpoint is disabled in production"))
    }
}
