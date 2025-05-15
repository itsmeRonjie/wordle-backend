package itsme

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@ComponentScan(basePackages = ["itsme"])
class WordleApplication

fun main(args: Array<String>) {
    runApplication<WordleApplication>(*args)
}
