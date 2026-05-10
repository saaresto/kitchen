package com.saaresto.kitchen.kitchenadmin.config

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Configuration
@ConditionalOnProperty(name = ["spring.main.lazy-initialization"], havingValue = "true")
class ExposedDatabaseInitializer(
    @Autowired private val dataSource: DataSource
) {

    private val logger = LoggerFactory.getLogger(ExposedDatabaseInitializer::class.java)

    @EventListener(ContextRefreshedEvent::class)
    fun initializeDatabase() {
        try {
            // Explicitly connect Exposed to the database
            // This will ensure the connection is available when lazy initialization is enabled
            Database.connect(dataSource)
            logger.info("Exposed database connection initialized successfully")
        } catch (e: Exception) {
            logger.warn("Database connection may already be initialized or failed to initialize: ${e.message}")
            // Don't throw exception to avoid breaking the application startup
            // The error will be caught when actual database operations are attempted
        }
    }
}
