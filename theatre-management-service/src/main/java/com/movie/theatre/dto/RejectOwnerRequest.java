package com.movie.theatre.dto;

/**
 * Request DTO for rejecting a theatre owner application.
 */
public class RejectOwnerRequest {

    private String reason;

    public RejectOwnerRequest() {
    }

    public RejectOwnerRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
