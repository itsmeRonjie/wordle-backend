package itsme.config

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.WebRequest

@Configuration
class ErrorConfig {
    
    @Bean
    fun errorAttributes(): DefaultErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): Map<String, Any> {
                val errorAttributes = super.getErrorAttributes(webRequest, options)
                return errorAttributes
            }
        }
    }
}
