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
    	List<Screen> screens = screenRepository.findByTheatre_Id(theatreId);
    	if (screens.isEmpty()) {
            throw new ResourceNotFoundException("No screens found for this theatre");
        }

        return screens;

    }
    
    public Screen updateStatus(Long screenId, Screen.Status status) {
        Screen screen = screenRepository.findById(screenId)
        		.orElseThrow(() -> new ResourceNotFoundException("screen not found"));

        screen.setStatus(status);
        return screenRepository.save(screen);
    }
    
    public List<Screen> getActiveScreensByTheatre(Long theatreId) {
    	List<Screen> screens = screenRepository.findByTheatre_IdAndStatus(
                theatreId, Screen.Status.ACTIVE
        );

        if (screens.isEmpty()) {
            throw new ResourceNotFoundException("No active screens found for this theatre");
        }

        return screens;

    }

}
