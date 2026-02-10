package com.cdac.booking.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to capture movie details from Catalogue Service.
 * Used in ticket response enrichment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {
    private Long id;
    private String title;
    // Only include fields we need for ticket display
}
