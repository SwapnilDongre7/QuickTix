package com.cdac.booking.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO to capture show details from ShowSeat Service.
 * Used in ticket response enrichment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponseDto {
    private String id;
    private Long movieId;
    private Long theatreId;
    private Long screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String language;
    private String format;
}
