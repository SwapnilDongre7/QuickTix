package com.quicktix.catalogue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


// this is temporary security config to allow all requests
// need to implement proper security later

@Configuration
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/movies/**").permitAll()   // TEMP FOR CLOUDINARY TEST
//                .requestMatchers("/genres/**").permitAll()
//                .requestMatchers("/languages/**").permitAll()
//                .requestMatchers("/cities/**").permitAll()
//                .anyRequest().permitAll()
//            )
//            .sessionManagement(sess -> 
//                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            );
//
//        return http.build();
//    }
	
	
	// TEMPORARY - ALLOW ALL REQUESTS
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth

                .anyRequest().permitAll()
                
            );
        return http.build();
    }
}
