package com.movie.theatre.dto;

import jakarta.validation.constraints.NotNull;

public class RegisterOwnerRequest {

    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
