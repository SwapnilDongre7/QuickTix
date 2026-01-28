package com.quicktix.showseat_service.dto.response;

import java.time.LocalDateTime;

import com.quicktix.showseat_service.enums.ShowStatus;
import com.quicktix.showseat_service.model.document.Pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    
    private String id;
    private Long movieId;
    private Long theatreId;
    private Long screenId;
    private String layoutId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Pricing pricing;
    private ShowStatus status;
    private String language;
    private String format;
    private Integer availableSeats;
    private Integer bookedSeats;
    private Boolean bookingEnabled;
    private LocalDateTime bookingCutoffTime;
    private String notes;
    private LocalDateTime createdAt;
}