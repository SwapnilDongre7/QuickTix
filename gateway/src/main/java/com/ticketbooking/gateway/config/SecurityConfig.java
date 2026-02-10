package com.ticketbooking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
                http
                                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                                                .pathMatchers("/ws/**").permitAll()
                                                .pathMatchers("/auth/**", "/movies/**", "/cities/**", "/genres/**",
                                                                "/languages/**",
                                                                "/theatres/active/**", "/theatres/city/**",
                                                                "/theatres/owner/**",
                                                                "/screens/active/**", "/api/shows/**", "/layouts/**",
                                                                "/seat-availability/**",
                                                                "/actuator/**", "/payments/webhook")
                                                .permitAll()
                                                .anyExchange().permitAll());

                return http.build();
        }
}
