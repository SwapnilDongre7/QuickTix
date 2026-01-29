package com.movie.theatre.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "theatre_owners")
public class TheatreOwner {

    /**
     * Application status for theatre owner requests.
     * PENDING - Awaiting admin review
     * APPROVED - Admin approved, user has THEATRE_OWNER role
     * REJECTED - Admin rejected the application
     */
    public enum ApplicationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long ownerId;

    // Refers to users.id from Identity Service
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Legacy column - kept for database backward compatibility
    @Column(nullable = false)
    private Boolean approved = false;

    // New status enum column - will be added to database via ddl-auto=update
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @PrePersist
    public void onCreate() {
        syncApprovedFromStatus();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        syncApprovedFromStatus();
    }

    // Keep approved column in sync with status
    private void syncApprovedFromStatus() {
        this.approved = (status == ApplicationStatus.APPROVED);
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        syncApprovedFromStatus();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    // Convenience getter for backward compatibility
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
