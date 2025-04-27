package com.saaresto.kitchen.kitchenadmin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Allow GET requests without authentication
                    .requestMatchers(HttpMethod.GET, "/api/visitors/**").permitAll()
                    // Require authentication for POST, PUT, DELETE requests
                    .requestMatchers(HttpMethod.POST, "/api/visitors/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/visitors/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/visitors/**").authenticated()

                    // Booking API security
                    .requestMatchers(HttpMethod.GET, "/api/bookings/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/bookings/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").authenticated()

                    // Public booking form
                    .requestMatchers("/bookings/create").permitAll()

                    // Secure admin panel
                    .requestMatchers("/admin/**").authenticated()

                    // Explicitly allow Swagger UI requests
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()

                    // Allow static resources
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                    // Allow all other requests
                    .anyRequest().permitAll()
            }
            .formLogin {
                it.loginPage("/login")
                it.defaultSuccessUrl("/admin/bookings/pending")
                it.permitAll()
            }
            .logout {
                it.logoutSuccessUrl("/login?logout")
                it.permitAll()
            }
            .httpBasic {}

        return http.build()
    }
}
