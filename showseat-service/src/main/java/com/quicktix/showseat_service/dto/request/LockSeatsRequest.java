package com.quicktix.showseat_service.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsRequest {
    
    @NotBlank(message = "Show ID is required")
    private String showId;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotNull(message = "Seat numbers are required")
    @Size(min = 1, max = 10, message = "You can lock between 1 and 10 seats")
    private List<@NotBlank(message = "Seat number cannot be blank") String> seatNumbers;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}