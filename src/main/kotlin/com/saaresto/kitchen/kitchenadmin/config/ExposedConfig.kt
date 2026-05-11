package com.saaresto.kitchen.kitchenadmin.config

import org.springframework.boot.LazyInitializationExcludeFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer

@Configuration
class ExposedConfig {

    @Bean
    fun lazyInitializationExcludeFilter(): LazyInitializationExcludeFilter {
        return LazyInitializationExcludeFilter.forBeanTypes(
            PlatformTransactionManager::class.java,
            Flyway::class.java,
            FlywayMigrationInitializer::class.java
        )
    }
}
