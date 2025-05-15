package itsme.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomErrorController : ErrorController {
    
    @GetMapping("/error", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleError(): Map<String, Any> {
        return mapOf(
            "status" to 404,
            "error" to "Not Found",
            "message" to "The requested resource was not found",
            "path" to "/error"
        )
    }
}
