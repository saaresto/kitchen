package com.saaresto.kitchen.kitchenadmin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

/**
 * Configuration for HTTP request logging.
 * This class configures a filter that logs all incoming HTTP requests.
 */
@Configuration
class LoggingConfig {

    /**
     * Creates a CommonsRequestLoggingFilter bean that logs incoming HTTP requests.
     * The filter is configured to include:
     * - Client IP address
     * - Request method and URI
     * - Request parameters
     * - Request headers
     * - Request body
     */
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeQueryString(true)
        loggingFilter.setIncludePayload(true)
        loggingFilter.setIncludeHeaders(true)
        loggingFilter.setMaxPayloadLength(10000) // Set maximum payload length to log
        return loggingFilter
    }
}