package com.saaresto.kitchen.kitchenadmin.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "basicAuth"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Kitchen Admin API")
                    .description("API for restaurant kitchen administration")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Kitchen Admin Team")
                            .email("admin@saaresto.com")
                    )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList(securitySchemeName)
            )
    }
}