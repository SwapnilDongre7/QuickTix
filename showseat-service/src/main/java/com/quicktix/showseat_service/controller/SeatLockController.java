package com.quicktix.showseat_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
import com.quicktix.showseat_service.dto.response.ApiResponse;
import com.quicktix.showseat_service.dto.response.LockSeatsResponse;
import com.quicktix.showseat_service.service.SeatLockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Slf4j
//@RestController
//@RequestMapping("/api/v1/seats")
//@RequiredArgsConstructor
//public class SeatLockController {
//
//    private final SeatLockService seatLockService;
//
//    /**
//     * Lock seats for a show
//     */
//    @PostMapping("/lock")
//    public ResponseEntity<LockSeatsResponse> lockSeats(
//            @Valid @RequestBody LockSeatsRequest request) {
//
//        log.info(
//            "Lock seats request: showId={}, userId={}, sessionId={}, seats={}",
//            request.getShowId(),
//            request.getUserId(),
//            request.getSessionId(),
//            request.getSeatNumbers()
//        );
//
//        LockSeatsResponse response = seatLockService.lockSeats(request);
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Unlock seats (manual / user cancellation)
//     */
//    @PostMapping("/unlock")
//    public ResponseEntity<Void> unlockSeats(
//            @Valid @RequestBody UnlockSeatsRequest request) {
//
//        log.info(
//            "Unlock seats request: showId={}, userId={}, sessionId={}, seats={}",
//            request.getShowId(),
//            request.getUserId(),
//            request.getSessionId(),
//            request.getSeatNumbers()
//        );
//
//        seatLockService.unlockSeats(request);
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * Confirm booked seats
//     */
//    @PostMapping("/confirm")
//    public ResponseEntity<Void> confirmSeats(
//            @Valid @RequestBody ConfirmSeatsRequest request) {
//
//        log.info(
//            "Confirm seats request: showId={}, bookingId={}, userId={}, sessionId={}, seats={}",
//            request.getShowId(),
//            request.getBookingId(),
//            request.getUserId(),
//            request.getSessionId(),
//            request.getSeatNumbers()
//        );
//
//        seatLockService.confirmSeats(request);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
//
//    /**
//     * Expire all locks for a show (scheduler / admin use)
//     */
//    @DeleteMapping("/expire/{showId}")
//    public ResponseEntity<Void> expireLocks(@PathVariable String showId) {
//
//        log.warn("Expiring all seat locks for showId={}", showId);
//
//        seatLockService.expireLocks(showId);
//        return ResponseEntity.noContent().build();
//    }
//}


@Slf4j
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Lock Management", description = "APIs for locking, unlocking, and confirming seat bookings")
public class SeatLockController {

    private final SeatLockService seatLockService;

    @PostMapping("/lock")
    @Operation(summary = "Lock seats", description = "Temporarily lock seats for a user during booking process")
    public ResponseEntity<ApiResponse<LockSeatsResponse>> lockSeats(
            @Valid @RequestBody LockSeatsRequest request) {

        log.info("Lock seats request: showId={}, userId={}, sessionId={}, seats={}",
                request.getShowId(), request.getUserId(), request.getSessionId(), request.getSeatNumbers());

        LockSeatsResponse response = seatLockService.lockSeats(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Seats locked successfully"));
    }

    @PostMapping("/unlock")
    @Operation(summary = "Unlock seats", description = "Release locked seats (user cancellation or timeout)")
    public ResponseEntity<ApiResponse<Void>> unlockSeats(
            @Valid @RequestBody UnlockSeatsRequest request) {

        log.info("Unlock seats request: showId={}, userId={}, sessionId={}, seats={}",
                request.getShowId(), request.getUserId(), request.getSessionId(), request.getSeatNumbers());

        seatLockService.unlockSeats(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Seats unlocked successfully"));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm booking", description = "Confirm seat booking after successful payment")
    public ResponseEntity<ApiResponse<Void>> confirmSeats(
            @Valid @RequestBody ConfirmSeatsRequest request) {

        log.info("Confirm seats request: showId={}, bookingId={}, userId={}, sessionId={}, seats={}",
                request.getShowId(), request.getBookingId(), request.getUserId(), 
                request.getSessionId(), request.getSeatNumbers());

        seatLockService.confirmSeats(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Seats confirmed successfully"));
    }

    @DeleteMapping("/expire/{showId}")
    @Operation(summary = "Expire locks", description = "Expire all seat locks for a show (admin/scheduler use)")
    public ResponseEntity<ApiResponse<Void>> expireLocks(
            @Parameter(description = "Show ID") @PathVariable String showId) {

        log.warn("Expiring all seat locks for showId={}", showId);

        seatLockService.expireLocks(showId);
        return ResponseEntity.ok(ApiResponse.success(null, "All locks expired for show"));
    }
}