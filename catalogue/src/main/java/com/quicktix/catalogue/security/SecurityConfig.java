package com.quicktix.catalogue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for Catalogue Service.
 * Implements JWT-based authentication with defense-in-depth model.
 * 
 * Public endpoints (READ operations for catalogue browsing):
 * - GET /movies/** - Public movie listings
 * - GET /genres/** - Public genre listings
 * - GET /languages/** - Public language listings
 * - GET /cities/** - Public city listings
 * - /v3/api-docs/**, /swagger-ui/** - OpenAPI documentation
 * - /actuator/** - Health checks
 * 
 * Protected endpoints (WRITE operations require ADMIN or THEATRE_OWNER role):
 * - POST, PUT, DELETE on catalogue resources
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // Public READ access for catalogue browsing
                        .requestMatchers(HttpMethod.GET, "/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/genres/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/languages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cities/**").permitAll()
                        // All write operations require authentication
                        .anyRequest().authenticated());

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
