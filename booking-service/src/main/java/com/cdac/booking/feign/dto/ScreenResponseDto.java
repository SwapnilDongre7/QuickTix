package com.cdac.booking.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to capture screen details from Theatre Service.
 * Used in ticket response enrichment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenResponseDto {
    private Long screenId;
    private String name;
}
