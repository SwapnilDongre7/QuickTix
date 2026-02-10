package com.movie.theatre.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, String path) {
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
