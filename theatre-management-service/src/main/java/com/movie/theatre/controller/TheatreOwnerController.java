package com.movie.theatre.controller;

import com.movie.theatre.dto.RegisterOwnerRequest;
import com.movie.theatre.dto.RejectOwnerRequest;
import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.service.TheatreOwnerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for theatre owner application management.
 * 
 * Flow:
 * 1. USER applies to become a theatre owner via POST /owners/register
 * 2. ADMIN views pending applications via GET /owners/pending
 * 3. ADMIN approves via PUT /owners/approve/{ownerId} OR rejects via PUT
 * /owners/reject/{ownerId}
 * 4. On approval, Identity Service is called to add THEATRE_OWNER role
 */
@RestController
@RequestMapping("/owners")
public class TheatreOwnerController {

    private final TheatreOwnerService theatreOwnerService;

    public TheatreOwnerController(TheatreOwnerService theatreOwnerService) {
        this.theatreOwnerService = theatreOwnerService;
    }

    /**
     * Apply to become a theatre owner.
     * Requires USER role. Creates a PENDING application.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TheatreOwner> register(@Valid @RequestBody RegisterOwnerRequest request) {
        TheatreOwner owner = theatreOwnerService.registerOwner(request.getUserId());
        return ResponseEntity.ok(owner);
    }

    /**
     * Get all pending theatre owner applications.
     * ADMIN only.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TheatreOwner>> getPendingApplications() {
        return ResponseEntity.ok(theatreOwnerService.getPendingApplications());
    }

    /**
     * Approve a theatre owner application.
     * ADMIN only. Calls Identity Service to add THEATRE_OWNER role.
     */
    @PutMapping("/approve/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheatreOwner> approve(@PathVariable("ownerId") Long ownerId) {
        return ResponseEntity.ok(theatreOwnerService.approveOwner(ownerId));
    }

    /**
     * Reject a theatre owner application.
     * ADMIN only.
     */
    @PutMapping("/reject/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheatreOwner> reject(@PathVariable("ownerId") Long ownerId,
            @RequestBody(required = false) RejectOwnerRequest request) {
        String reason = (request != null) ? request.getReason() : null;
        return ResponseEntity.ok(theatreOwnerService.rejectOwner(ownerId, reason));
    }

    /**
     * Get a theatre owner by ID.
     */
    @GetMapping("/{ownerId}")
    public ResponseEntity<TheatreOwner> getById(@PathVariable("ownerId") Long ownerId) {
        return ResponseEntity.ok(theatreOwnerService.getById(ownerId));
    }

    /**
     * Get a theatre owner by user ID.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<TheatreOwner> getByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(theatreOwnerService.getByUserId(userId));
    }
}
