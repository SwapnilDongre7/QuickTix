package com.movie.theatre.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * RestTemplate interceptor that automatically propagates the Authorization
 * header
 * from incoming requests to outgoing inter-service calls.
 * This ensures that the authentication context is maintained across service
 * boundaries.
 */
@Component
public class AuthTokenPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest servletRequest = attrs.getRequest();
            String authHeader = servletRequest.getHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                request.getHeaders().set("Authorization", authHeader);
            }
        }
        return execution.execute(request, body);
    }
}
