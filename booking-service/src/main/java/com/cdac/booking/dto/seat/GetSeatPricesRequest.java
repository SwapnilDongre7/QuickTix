package com.cdac.booking.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for fetching seat prices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSeatPricesRequest {
    private List<String> seatNumbers;
}
