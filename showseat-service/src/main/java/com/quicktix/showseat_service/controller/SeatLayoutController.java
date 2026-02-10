package com.quicktix.showseat_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.showseat_service.dto.request.CreateLayoutRequest;
import com.quicktix.showseat_service.dto.response.ApiResponse;
import com.quicktix.showseat_service.dto.response.SeatLayoutResponse;
import com.quicktix.showseat_service.service.SeatLayoutService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/layouts")
@RequiredArgsConstructor
@Tag(name = "Seat Layout Management", description = "APIs for managing seat layouts")
public class SeatLayoutController {

    private final SeatLayoutService layoutService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATRE_OWNER')")
    @Operation(summary = "Create seat layout", description = "Create a new seat layout for a screen")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> createLayout(
            @Valid @RequestBody CreateLayoutRequest request,
            @Parameter(description = "User ID creating the layout") @RequestHeader("X-User-Id") Long userId) {

        SeatLayoutResponse response = layoutService.createLayout(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Seat layout created successfully"));
    }

    @GetMapping("/{layoutId}")
    @Operation(summary = "Get layout by ID", description = "Retrieve seat layout details by ID")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> getLayoutById(
            @Parameter(description = "Layout ID") @PathVariable String layoutId) {

        SeatLayoutResponse response = layoutService.getLayoutById(layoutId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/screen/{screenId}")
    @Operation(summary = "Get layouts by screen", description = "Retrieve all seat layouts for a screen")
    public ResponseEntity<ApiResponse<List<SeatLayoutResponse>>> getLayoutsByScreen(
            @Parameter(description = "Screen ID") @PathVariable Long screenId) {

        List<SeatLayoutResponse> response = layoutService.getLayoutsByScreen(screenId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/screen/{screenId}/active")
    @Operation(summary = "Get active layout", description = "Retrieve the active seat layout for a screen")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> getActiveLayout(
            @Parameter(description = "Screen ID") @PathVariable Long screenId) {

        SeatLayoutResponse response = layoutService.getActiveLayoutByScreen(screenId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{layoutId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATRE_OWNER')")
    @Operation(summary = "Deactivate layout", description = "Mark a seat layout as inactive")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> deactivateLayout(
            @Parameter(description = "Layout ID") @PathVariable String layoutId) {

        SeatLayoutResponse response = layoutService.deactivateLayout(layoutId);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Layout deactivated successfully"));
    }

    @DeleteMapping("/{layoutId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATRE_OWNER')")
    @Operation(summary = "Delete layout", description = "Delete a seat layout by ID")
    public ResponseEntity<ApiResponse<Void>> deleteLayout(
            @Parameter(description = "Layout ID") @PathVariable String layoutId) {

        layoutService.deleteLayout(layoutId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Layout deleted successfully"));
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATRE_OWNER')")
    @Operation(summary = "Get layouts by owner", description = "Retrieve all reusable seat layouts created by an owner")
    public ResponseEntity<ApiResponse<List<SeatLayoutResponse>>> getLayoutsByOwner(
            @Parameter(description = "Owner ID") @PathVariable Long ownerId) {

        List<SeatLayoutResponse> response = layoutService.getLayoutsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATRE_OWNER')")
    @Operation(summary = "Get all layouts", description = "Retrieve all active seat layouts")
    public ResponseEntity<ApiResponse<List<SeatLayoutResponse>>> getAllLayouts() {

        List<SeatLayoutResponse> response = layoutService.getAllLayouts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
