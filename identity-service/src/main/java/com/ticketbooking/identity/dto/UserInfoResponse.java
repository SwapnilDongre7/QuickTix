package com.ticketbooking.identity.dto;

/**
 * Response DTO for user info queries.
 * Used by Theatre Service to display user details in pending applications.
 */
public class UserInfoResponse {

    private Long userId;
    private String name;
    private String email;

    public UserInfoResponse() {
    }

    public UserInfoResponse(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
