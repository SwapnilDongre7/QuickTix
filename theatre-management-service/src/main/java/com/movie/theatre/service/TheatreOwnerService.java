package com.movie.theatre.service;

import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.repository.TheatreOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TheatreOwnerService {

    private final TheatreOwnerRepository theatreOwnerRepository;

    public TheatreOwnerService(TheatreOwnerRepository theatreOwnerRepository) {
        this.theatreOwnerRepository = theatreOwnerRepository;
    }

    // Register a new theatre owner (by userId from Identity Service)
    public TheatreOwner registerOwner(Long userId) {
        Optional<TheatreOwner> existing = theatreOwnerRepository.findByUserId(userId);
        if (existing.isPresent()) {
            throw new RuntimeException("User is already a theatre owner");
        }

        TheatreOwner owner = new TheatreOwner();
        owner.setUserId(userId);
        owner.setApproved(false);

        return theatreOwnerRepository.save(owner);
    }

    // Admin approves owner
    public TheatreOwner approveOwner(Long ownerId) {
        TheatreOwner owner = theatreOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found"));

        owner.setApproved(true);
        return theatreOwnerRepository.save(owner);
    }

    public TheatreOwner getByUserId(Long userId) {
        return theatreOwnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Theatre owner not found for user"));
    }
}
