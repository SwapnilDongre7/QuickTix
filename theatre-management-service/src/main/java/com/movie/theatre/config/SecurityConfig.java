//package com.movie.theatre.config;
//
//import com.movie.theatre.security.JwtAuthenticationFilter;
//import com.movie.theatre.security.JwtUtil;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
///**
// * Security configuration for Theatre Management Service.
// * Implements JWT-based authentication with role-based access control.
// * 
// * Role-based endpoints:
// * - /owners/register - USER applies to become theatre owner
// * - /owners/pending, /owners/approve/**, /owners/reject/** - ADMIN only
// * - /theatres (POST), /screens (POST) - THEATRE_OWNER only
// * - /theatres/status, /screens/status - ADMIN or THEATRE_OWNER
// */
//@Configuration
//@EnableMethodSecurity
//public class SecurityConfig {
//
//        @Bean
//        public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
//
//                http.csrf(csrf -> csrf.disable());
//
//                http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//                http.authorizeHttpRequests(auth -> auth
//                                // Theatre Owner Application Flow
//                                .requestMatchers("/owners/register").hasRole("USER")
//                                .requestMatchers("/owners/pending").hasRole("ADMIN")
//                                .requestMatchers("/owners/approve/**").hasRole("ADMIN")
//                                .requestMatchers("/owners/reject/**").hasRole("ADMIN")
//
//                                // Public Theatre & Screen Access
//                                .requestMatchers(HttpMethod.GET, "/theatres/active/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/theatres/city/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/theatres/owner/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/screens/active/**").permitAll()
//                                // Public Owner Profile Access
//                                .requestMatchers(HttpMethod.GET, "/owners/{ownerId}").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/owners/user/{userId}").permitAll()
//
//                                // Secure Status Updates
//                                .requestMatchers(HttpMethod.PUT, "/theatres/status")
//                                .hasAnyRole("ADMIN", "THEATRE_OWNER")
//                                .requestMatchers(HttpMethod.PUT, "/screens/status").hasAnyRole("ADMIN", "THEATRE_OWNER")
//
//                                // Theatre & Screen Management (Create operations)
//                                .requestMatchers(HttpMethod.POST, "/theatres").hasRole("THEATRE_OWNER")
//                                .requestMatchers(HttpMethod.POST, "/screens").hasRole("THEATRE_OWNER")
//
//                                // View All Theatres
//                                .requestMatchers(HttpMethod.GET, "/theatres").hasAnyRole("ADMIN", "THEATRE_OWNER")
//
//                                // Actuator endpoints
//                                .requestMatchers("/actuator/**").permitAll()
//
//                                // All other endpoints require authentication
//                                .anyRequest().authenticated());
//
//                http.addFilterBefore(
//                                new JwtAuthenticationFilter(jwtUtil),
//                                UsernamePasswordAuthenticationFilter.class);
//
//                return http.build();
//        }
//}

package com.movie.theatre.config;

import com.movie.theatre.security.JwtAuthenticationFilter;
import com.movie.theatre.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for Theatre Management Service.
 * Implements JWT-based authentication with role-based access control.
 * 
 * Role-based endpoints:
 * - /owners/register - USER applies to become theatre owner
 * - /owners/pending, /owners/approve/**, /owners/reject/** - ADMIN only
 * - /theatres (POST), /screens (POST) - THEATRE_OWNER only
 * - /theatres/status, /screens/status - ADMIN or THEATRE_OWNER
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {

                http.csrf(csrf -> csrf.disable());

                http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.authorizeHttpRequests(auth -> auth
                                // Theatre Owner Application Flow
                                // Any authenticated user can apply to become owner
                                .requestMatchers("/owners/register").hasAnyRole("USER", "THEATRE_OWNER", "ADMIN")
                                .requestMatchers("/owners/pending").hasRole("ADMIN")
                                .requestMatchers("/owners/approve/**").hasRole("ADMIN")
                                .requestMatchers("/owners/reject/**").hasRole("ADMIN")

                                // Public Theatre & Screen Access
                                .requestMatchers(HttpMethod.GET, "/theatres/active/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/theatres/city/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/theatres/owner/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/screens/active/**").permitAll()
                                // Public Owner Profile Access
                                .requestMatchers(HttpMethod.GET, "/owners/{ownerId}").permitAll()
                                .requestMatchers(HttpMethod.GET, "/owners/user/{userId}").permitAll()

                                // Secure Status Updates
                                .requestMatchers(HttpMethod.PUT, "/theatres/status")
                                .hasAnyRole("ADMIN", "THEATRE_OWNER")
                                .requestMatchers(HttpMethod.PUT, "/screens/status").hasAnyRole("ADMIN", "THEATRE_OWNER")

                                // Theatre & Screen Management (Create operations)
                                .requestMatchers(HttpMethod.POST, "/theatres").hasRole("THEATRE_OWNER")
                                .requestMatchers(HttpMethod.POST, "/screens").hasRole("THEATRE_OWNER")

                                // View All Theatres
                                .requestMatchers(HttpMethod.GET, "/theatres").hasAnyRole("ADMIN", "THEATRE_OWNER")

                                // Actuator endpoints
                                .requestMatchers("/actuator/**").permitAll()

                                // All other endpoints require authentication
                                .anyRequest().authenticated());

                http.addFilterBefore(
                                new JwtAuthenticationFilter(jwtUtil),
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}

