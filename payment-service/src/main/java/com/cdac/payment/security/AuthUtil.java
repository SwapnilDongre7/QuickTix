package com.cdac.payment.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static String getCurrentUser() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public static boolean hasRole(String role) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}		