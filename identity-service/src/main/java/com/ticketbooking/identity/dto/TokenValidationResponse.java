package com.ticketbooking.identity.dto;

import java.util.List;

public class TokenValidationResponse {

    private Long userId;
    private String email;
    private List<String> roles;
    private boolean valid;

    public TokenValidationResponse(Long userId, String email, List<String> roles, boolean valid) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.valid = valid;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isValid() {
        return valid;
    }
}
