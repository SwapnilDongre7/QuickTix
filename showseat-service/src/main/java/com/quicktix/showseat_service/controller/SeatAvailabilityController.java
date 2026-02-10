package com.quicktix.showseat_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.showseat_service.dto.response.ApiResponse;
import com.quicktix.showseat_service.dto.response.SeatAvailabilityResponse;
import com.quicktix.showseat_service.service.SeatAvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/seat-availability")
@RequiredArgsConstructor
@Tag(name = "Seat Availability", description = "APIs for seat availability and status")
public class SeatAvailabilityController {

    private final SeatAvailabilityService seatAvailabilityService;

    @GetMapping("/show/{showId}")
    @Operation(summary = "Get seat availability for show", description = "Retrieve seat availability and status for a given show")
    public ResponseEntity<ApiResponse<SeatAvailabilityResponse>> getSeatAvailability(
            @Parameter(description = "Show ID") @PathVariable String showId) {

        SeatAvailabilityResponse response = seatAvailabilityService.getSeatAvailability(showId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/show/{showId}/seat/{seatNo}/available")
    @Operation(summary = "Check seat availability", description = "Check whether a specific seat is available for a show")
    public ResponseEntity<ApiResponse<Boolean>> isSeatAvailable(
            @Parameter(description = "Show ID") @PathVariable String showId,
            @Parameter(description = "Seat number") @PathVariable String seatNo) {

        boolean available = seatAvailabilityService.isSeatAvailable(showId, seatNo);

        return ResponseEntity.ok(ApiResponse.success(available));
    }

    @GetMapping("/show/{showId}/compact")
    @Operation(summary = "Get compressed seat availability", description = "Retrieve a Base64 encoded bitmask of seat statuses (2 bits per seat)")
    public ResponseEntity<ApiResponse<String>> getCompressedAvailability(
            @Parameter(description = "Show ID") @PathVariable String showId) {

        String response = seatAvailabilityService.getCompressedAvailability(showId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}