package com.movie.theatre.dto;

import com.movie.theatre.entity.TheatreOwner;

/**
 * DTO for pending theatre owner applications with enriched user info.
 */
public class PendingApplicationDTO {

    private Long ownerId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String status;
    private String createdAt;

    public PendingApplicationDTO() {
    }

    public PendingApplicationDTO(TheatreOwner owner, String userName, String userEmail) {
        this.ownerId = owner.getOwnerId();
        this.userId = owner.getUserId();
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = owner.getStatus() != null ? owner.getStatus().name() : "PENDING";
        this.createdAt = owner.getCreatedAt() != null ? owner.getCreatedAt().toString() : null;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
