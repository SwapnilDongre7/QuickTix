package com.ticketbooking.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

/**
 * JWT utility for token validation and claims extraction
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Validate JWT token
     * 
     * @param token JWT token
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all claims from token
     * 
     * @param token JWT token
     * @return Claims object
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT token
     * @return User ID as string
     */
    public String extractUserId(String token) {
        Claims claims = extractClaims(token);
        // Try different possible claim names for user ID
        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.get("user_id");
        }
        if (userId == null) {
            userId = claims.get("sub");
        }
        if (userId == null) {
            userId = claims.getSubject();
        }
        return userId != null ? userId.toString() : null;
    }

    /**
     * Extract username from token
     * 
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from token
     * 
     * @param token JWT token
     * @return Comma-separated roles string
     */
    @SuppressWarnings("unchecked")
    public String extractRoles(String token) {
        Claims claims = extractClaims(token);
        Object roles = claims.get("roles");
        if (roles == null) {
            roles = claims.get("authorities");
        }
        if (roles == null) {
            roles = claims.get("scope");
        }

        if (roles instanceof List) {
            return String.join(",", (List<String>) roles);
        } else if (roles instanceof String) {
            return (String) roles;
        }
        return "";
    }

    /**
     * Extract email from token
     * 
     * @param token JWT token
     * @return Email address
     */
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        Object email = claims.get("email");
        return email != null ? email.toString() : null;
    }
}
