package com.quicktix.showseat_service.config;

import com.quicktix.showseat_service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for ShowSeat Service.
 * Implements JWT-based authentication with defense-in-depth model.
 * 
 * Public endpoints:
 * - GET /shows/** - Public show listings for browsing
 * - /v3/api-docs/**, /swagger-ui/** - OpenAPI documentation
 * - /actuator/** - Health checks
 * 
 * Protected endpoints:
 * - POST, PUT, DELETE on shows (THEATRE_OWNER, ADMIN)
 * - Seat locking/booking operations (require USER authentication)
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Swagger endpoints
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						// Actuator endpoints
						.requestMatchers("/actuator/**").permitAll()
						// Public GET access for show browsing
						.requestMatchers(HttpMethod.GET, "/shows/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/pricing/**").permitAll()
						// All other endpoints require authentication
						.anyRequest().authenticated());

		// Add JWT filter before UsernamePasswordAuthenticationFilter
		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
