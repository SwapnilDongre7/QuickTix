package com.ticketbooking.gateway.filter;

import com.ticketbooking.gateway.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter for API Gateway
 * 
 * This filter performs the following:
 * 1. Validates the JWT token
 * 2. Extracts user information (userId, roles, email)
 * 3. Forwards user info via headers to downstream services
 * 
 * By doing this, downstream services don't need to re-validate the JWT.
 * They can trust the headers from the Gateway.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Header names for forwarding user info
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_AUTH_VALIDATED = "X-Auth-Validated";

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Allow public endpoints without authentication
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            System.out.println("Invalid token detected at Gateway");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        System.out.println("Valid token passed Gateway validation");

        // Extract user information from JWT
        String userId = jwtUtil.extractUserId(token);
        String roles = jwtUtil.extractRoles(token);
        String email = jwtUtil.extractEmail(token);

        // Build mutated request with user info headers
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER_AUTH_VALIDATED, "true")
                .header(HEADER_USER_ID, userId != null ? userId : "")
                .header(HEADER_USER_ROLES, roles != null ? roles : "")
                .header(HEADER_USER_EMAIL, email != null ? email : "")
                .build();

        // Create new exchange with mutated request
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        System.out.println("Forwarding request with headers - UserId: " + userId + ", Roles: " + roles);

        return chain.filter(mutatedExchange);
    }

    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/actuator")
                || path.startsWith("/payments/webhook"); // Razorpay webhooks don't have JWT
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
