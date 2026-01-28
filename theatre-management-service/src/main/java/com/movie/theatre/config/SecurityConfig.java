//package com.movie.theatre.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()
//            );
//        return http.build();
//    }
//}

package com.movie.theatre.config;

import com.movie.theatre.security.JwtAuthenticationFilter;
import com.movie.theatre.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {

                http.csrf(csrf -> csrf.disable());

                http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/owners/register").hasRole("USER")
                                .requestMatchers("/owners/approve/**").hasRole("ADMIN")

                                // Secure Status Updates (Critical Fix)
                                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/theatres/status")
                                .hasAnyRole("ADMIN", "THEATRE_OWNER")
                                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/screens/status")
                                .hasAnyRole("ADMIN", "THEATRE_OWNER")

                                // Theatre & Screen Management
                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/theatres")
                                .hasRole("THEATRE_OWNER")
                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/screens")
                                .hasRole("THEATRE_OWNER")

                                // View All Theatres (Admin & Owner primarily, or specific logic)
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/theatres")
                                .hasAnyRole("ADMIN", "THEATRE_OWNER")

                                // Public browsing endpoints (can be authenticated users or permitAll depending
                                // on req, keeping authenticated for now as per previous config)
                                .anyRequest().authenticated());

                http.addFilterBefore(
                                new JwtAuthenticationFilter(jwtUtil),
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
