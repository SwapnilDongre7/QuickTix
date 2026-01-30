package com.ticketbooking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        public SecurityConfig() {
        }

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
                http
                                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                                                .pathMatchers("/auth/**", "/movies/**", "/cities/**", "/genres/**",
                                                                "/languages/**",
                                                                "/theatres/active/**", "/theatres/city/**",
                                                                "/theatres/owner/**",
                                                                "/screens/active/**", "/api/shows/**", "/layouts/**",
                                                                "/seat-availability/**",
                                                                "/actuator/**", "/payments/webhook")
                                                .permitAll()
                                                .anyExchange().permitAll());
                // .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION); //
                // JwtAuthenticationFilter is a GlobalFilter, not a WebFilter. We rely on it for
                // AuthZ.

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Use allowedOriginPatterns for wildcard support with credentials
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "https://*.ngrok-free.dev",
                                "https://*.ngrok.io"));
                configuration.setAllowedMethods(
                                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
                configuration.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "X-Request-Id",
                                "Content-Disposition"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
