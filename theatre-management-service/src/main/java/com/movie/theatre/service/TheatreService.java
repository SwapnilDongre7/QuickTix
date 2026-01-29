package com.movie.theatre.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.movie.theatre.entity.Screen;
import com.movie.theatre.entity.Theatre;
import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.exception.ResourceNotFoundException;
import com.movie.theatre.exception.UnauthorizedOperationException;
import com.movie.theatre.repository.ScreenRepository;
import com.movie.theatre.repository.TheatreOwnerRepository;
import com.movie.theatre.repository.TheatreRepository;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final TheatreOwnerRepository theatreOwnerRepository;
    private final ScreenRepository screenRepository;

    public TheatreService(TheatreRepository theatreRepository, TheatreOwnerRepository theatreOwnerRepository,
            ScreenRepository screenRepository) {
        this.theatreRepository = theatreRepository;
        this.theatreOwnerRepository = theatreOwnerRepository;
        this.screenRepository = screenRepository;
    }

    // Owner creates a theatre
    public Theatre createTheatre(Long ownerId, Theatre theatre) {
        TheatreOwner owner = theatreOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre Owner not found"));

        if (!Boolean.TRUE.equals(owner.getApproved())) {
            throw new UnauthorizedOperationException("Theatre owner not approved yet");
        }

        theatre.setOwner(owner);
        return theatreRepository.save(theatre);
    }

    public List<Theatre> getTheatresByOwner(Long ownerId) {
        List<Theatre> theatres = theatreRepository.findByOwner_OwnerId(ownerId);
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No theatres found for this owner");
        }
        return theatres;
    }

    public List<Theatre> getTheatresByCity(Integer cityId) {
        List<Theatre> theatres = theatreRepository.findByCityId(cityId);
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No theatres found in this city");
        }
        return theatres;
    }

    // public Theatre updateStatus(Long theatreId, Theatre.Status status) {
    // Theatre theatre = theatreRepository.findById(theatreId)
    // .orElseThrow(() -> new RuntimeException("Theatre not found"));
    //
    // theatre.setStatus(status);
    // return theatreRepository.save(theatre);
    // }

    public List<Theatre> getActiveTheatresByCity(Integer cityId) {
        List<Theatre> theatres = theatreRepository.findByCityIdAndStatus(cityId, Theatre.Status.ACTIVE);
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No active theatres found in this city");
        }
        return theatres;
    }

    public List<Theatre> getActiveTheatresByOwner(Long ownerId) {
        List<Theatre> theatres = theatreRepository.findByOwner_OwnerIdAndStatus(ownerId, Theatre.Status.ACTIVE);
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No active theatres found for this owner");
        }
        return theatres;
    }

    public List<Theatre> getAllTheatres() {
        List<Theatre> theatres = theatreRepository.findAll();
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No theatres found");
        }
        return theatres;
    }

    public List<Theatre> getAllActiveTheatres() {
        List<Theatre> theatres = theatreRepository.findAll()
                .stream()
                .filter(t -> t.getStatus() == Theatre.Status.ACTIVE)
                .toList();
        if (theatres.isEmpty()) {
            throw new ResourceNotFoundException("No active theatres found");
        }
        return theatres;
    }

    public Theatre updateStatus(Long theatreId, Theatre.Status status, Long userId, List<String> roles) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

        // Authorization Check
        boolean isAdmin = roles.contains("ADMIN");
        boolean isOwner = roles.contains("THEATRE_OWNER");

        if (!isAdmin) {
            if (!isOwner) {
                throw new UnauthorizedOperationException("User is not authorized to update theatre status");
            }
            // Check if this user owns the theatre
            if (!theatre.getOwner().getUserId().equals(userId)) {
                throw new UnauthorizedOperationException("You can only update status of your own theatres");
            }
        }

        theatre.setStatus(status);

        // If theatre is being deactivated, deactivate all screens
        if (status == Theatre.Status.INACTIVE) {
            List<Screen> screens = screenRepository.findByTheatre_TheatreId(theatreId);
            for (Screen screen : screens) {
                screen.setStatus(Screen.Status.INACTIVE);
            }
            screenRepository.saveAll(screens);
        }

        return theatreRepository.save(theatre);
    }

}
