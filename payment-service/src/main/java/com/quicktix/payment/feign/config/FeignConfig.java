package com.quicktix.payment.feign.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration for JWT token propagation and error handling.
 */
@Configuration
public class FeignConfig {

    /**
     * Request interceptor to propagate Authorization header to downstream services.
     */
    @Bean
    public RequestInterceptor authorizationInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isEmpty()) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }

    /**
     * Custom error decoder for handling Feign client errors.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
