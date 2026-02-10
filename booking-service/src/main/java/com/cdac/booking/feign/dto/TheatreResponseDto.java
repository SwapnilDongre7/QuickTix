package com.cdac.booking.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to capture theatre details from Theatre Service.
 * Used in ticket response enrichment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheatreResponseDto {
    private Long theatreId;
    private String name;
    private String address;
}
