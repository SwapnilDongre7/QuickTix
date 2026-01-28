package com.movie.theatre.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.theatre.dto.CreateScreenRequest;
import com.movie.theatre.dto.UpdateScreenStatusRequest;
import com.movie.theatre.entity.Screen;
import com.movie.theatre.service.ScreenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/screens")
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @PostMapping
    public Screen add(@Valid @RequestBody CreateScreenRequest request) {
        Screen screen = new Screen();
        screen.setName(request.getName());
        screen.setCapacity(request.getCapacity());

        return screenService.addScreen(request.getTheatreId(), screen);
    }

    @GetMapping("/theatre/{theatreId}")
    public List<Screen> getByTheatre(@PathVariable("theatreId") Long theatreId) {
        return screenService.getScreensByTheatre(theatreId);
    }
    
    @PutMapping("/status")
    public Screen updateStatus(@Valid @RequestBody UpdateScreenStatusRequest request) {
        return screenService.updateStatus(
                request.getScreenId(),
                Screen.Status.valueOf(request.getStatus())
        );
    }
 // Public / Operational – ONLY ACTIVE
    @GetMapping("/active/theatre/{theatreId}")
    public List<Screen> getActiveByTheatre(@PathVariable("theatreId") Long theatreId) {
        return screenService.getActiveScreensByTheatre(theatreId);
    }

}
