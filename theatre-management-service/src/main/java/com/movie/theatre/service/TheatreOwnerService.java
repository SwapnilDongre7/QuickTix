package com.movie.theatre.service;

import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.entity.TheatreOwner.ApplicationStatus;
import com.movie.theatre.feign.IdentityFeignClient;
import com.movie.theatre.repository.TheatreOwnerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing theatre owner applications and approvals.
 * Uses OpenFeign for communication with Identity Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TheatreOwnerService {

    private final TheatreOwnerRepository theatreOwnerRepository;
    private final IdentityFeignClient identityFeignClient;

    /**
     * Register a new theatre owner application.
     */
    @Transactional
    public TheatreOwner registerOwner(Long userId) {
        log.info("Registering theatre owner for user: {}", userId);

        // Check if already exists
        if (theatreOwnerRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Theatre owner application already exists for user: " + userId);
        }

        TheatreOwner owner = new TheatreOwner();
        owner.setUserId(userId);
        owner.setStatus(ApplicationStatus.PENDING);

        TheatreOwner saved = theatreOwnerRepository.save(owner);
        log.info("Theatre owner application created: {}", saved.getOwnerId());
        return saved;
    }

    /**
     * Get all pending theatre owner applications.
     */
    public List<TheatreOwner> getPendingApplications() {
        log.info("Fetching pending theatre owner applications");
        return theatreOwnerRepository.findByStatus(ApplicationStatus.PENDING);
    }

    /**
     * Approve a theatre owner application.
     * This will add the THEATRE_OWNER role to the user in Identity Service.
     */
    @Transactional
    @CircuitBreaker(name = "identityService", fallbackMethod = "approveOwnerFallback")
    @Retry(name = "identityService")
    public TheatreOwner approveOwner(Long ownerId) {
        log.info("Approving theatre owner application: {}", ownerId);

        TheatreOwner owner = theatreOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found: " + ownerId));

        if (owner.getStatus() == ApplicationStatus.APPROVED) {
            throw new RuntimeException("Theatre owner already approved");
        }

        // Call Identity Service to add THEATRE_OWNER role via Feign
        try {
            log.info("Adding THEATRE_OWNER role to user {} via Feign", owner.getUserId());
            identityFeignClient.addRoleToUser(owner.getUserId(), "THEATRE_OWNER");
            log.info("Successfully added THEATRE_OWNER role to user {}", owner.getUserId());
        } catch (Exception e) {
            log.error("Failed to add role to user {}: {}", owner.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to update user role in Identity Service: " + e.getMessage());
        }

        // Update owner status
        owner.setStatus(ApplicationStatus.APPROVED);
        owner.setReviewedAt(LocalDateTime.now());
        owner.setRejectionReason(null);

        TheatreOwner savedOwner = theatreOwnerRepository.save(owner);
        log.info("Theatre owner {} approved successfully", ownerId);

        return savedOwner;
    }

    /**
     * Reject a theatre owner application.
     */
    @Transactional
    public TheatreOwner rejectOwner(Long ownerId, String reason) {
        log.info("Rejecting theatre owner application: {}, reason: {}", ownerId, reason);

        TheatreOwner owner = theatreOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found: " + ownerId));

        if (owner.getStatus() == ApplicationStatus.APPROVED) {
            throw new RuntimeException("Cannot reject an approved theatre owner");
        }

        owner.setStatus(ApplicationStatus.REJECTED);
        owner.setRejectionReason(reason);
        owner.setReviewedAt(LocalDateTime.now());

        TheatreOwner savedOwner = theatreOwnerRepository.save(owner);
        log.info("Theatre owner {} rejected", ownerId);

        return savedOwner;
    }

    /**
     * Get a theatre owner by ID.
     */
    public TheatreOwner getById(Long ownerId) {
        return theatreOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found: " + ownerId));
    }

    /**
     * Get a theatre owner by user ID.
     */
    public TheatreOwner getByUserId(Long userId) {
        return theatreOwnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found for user: " + userId));
    }

    /**
     * Fallback method when Identity service is unavailable.
     */
    @SuppressWarnings("unused")
    private TheatreOwner approveOwnerFallback(Long ownerId, Exception ex) {
        log.error("Circuit breaker: Identity service unavailable for approval. ownerId={}, error={}",
                ownerId, ex.getMessage());
        throw new RuntimeException("Identity service temporarily unavailable. Please try again later.");
    }
}
