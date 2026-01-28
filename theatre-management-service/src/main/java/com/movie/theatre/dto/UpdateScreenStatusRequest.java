package com.movie.theatre.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateScreenStatusRequest {

    @NotNull
    private Long screenId;

    @NotNull
    private String status; // ACTIVE or INACTIVE

    public Long getScreenId() {
        return screenId;
    }

    public void setScreenId(Long screenId) {
        this.screenId = screenId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
