package com.movie.theatre.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.movie.theatre.entity.Screen;
import com.movie.theatre.entity.Theatre;
import com.movie.theatre.exception.ResourceNotFoundException;
import com.movie.theatre.repository.ScreenRepository;
import com.movie.theatre.repository.TheatreRepository;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public ScreenService(ScreenRepository screenRepository,
            TheatreRepository theatreRepository) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
    }

    // Add a screen to a theatre
    public Screen addScreen(Long theatreId, Screen screen) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

        screen.setTheatre(theatre);
        return screenRepository.save(screen);
    }

    public List<Screen> getScreensByTheatre(Long theatreId) {
        List<Screen> screens = screenRepository.findByTheatre_TheatreId(theatreId);
        if (screens.isEmpty()) {
            throw new ResourceNotFoundException("No screens found for this theatre");
        }

        return screens;

    }

    public Screen updateStatus(Long screenId, Screen.Status status, Long userId, List<String> roles) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("screen not found"));

        // Authorization Check
        boolean isAdmin = roles.contains("ADMIN");
        boolean isOwner = roles.contains("THEATRE_OWNER");

        if (!isAdmin) {
            if (!isOwner) {
                throw new com.movie.theatre.exception.UnauthorizedOperationException(
                        "User is not authorized to update screen status");
            }
            // Check if this user owns the theatre this screen belongs to
            if (!screen.getTheatre().getOwner().getUserId().equals(userId)) {
                throw new com.movie.theatre.exception.UnauthorizedOperationException(
                        "You can only update status of screens in your own theatres");
            }
        }

        screen.setStatus(status);
        return screenRepository.save(screen);
    }

    /**
     * Get a screen by ID
     * Used for inter-service communication (Feign clients)
     */
    public Screen getScreenById(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));
    }

    public List<Screen> getActiveScreensByTheatre(Long theatreId) {
        List<Screen> screens = screenRepository.findByTheatre_TheatreIdAndStatus(
                theatreId, Screen.Status.ACTIVE);

        if (screens.isEmpty()) {
            throw new ResourceNotFoundException("No active screens found for this theatre");
        }

        return screens;

    }

}
