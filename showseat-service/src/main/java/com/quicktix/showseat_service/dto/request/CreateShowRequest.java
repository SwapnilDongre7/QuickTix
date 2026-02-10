package com.quicktix.showseat_service.dto.request;

import java.time.LocalDateTime;

import com.quicktix.showseat_service.model.document.Pricing;

import jakarta.validation.constraints.Future;
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
public class CreateShowRequest {
    
    @NotNull(message = "Movie ID is required")
    @Positive(message = "Movie ID must be positive")
    private Long movieId;
    
    @NotNull(message = "Theatre ID is required")
    @Positive(message = "Theatre ID must be positive")
    private Long theatreId;
    
    @NotNull(message = "Screen ID is required")
    @Positive(message = "Screen ID must be positive")
    private Long screenId;
    
    @NotBlank(message = "Layout ID is required")
    private String layoutId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Pricing is required")
    private Pricing pricing;
    
    @Size(max = 50, message = "Language cannot exceed 50 characters")
    private String language;
    
    @Size(max = 50, message = "Format cannot exceed 50 characters")
    private String format;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}