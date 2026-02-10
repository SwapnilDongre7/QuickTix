package com.movie.theatre.dto;

/**
 * Response DTO for user info from Identity Service.
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
