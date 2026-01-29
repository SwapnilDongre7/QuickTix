package com.quicktix.showseat_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for getting seat prices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSeatPricesRequest {

    private List<String> seatNumbers;
}
