package com.movie.theatre.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateTheatreStatusRequest {

    @NotNull
    private Long theatreId;

    @NotNull
    private String status; // ACTIVE or INACTIVE

    public Long getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Long theatreId) {
        this.theatreId = theatreId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
