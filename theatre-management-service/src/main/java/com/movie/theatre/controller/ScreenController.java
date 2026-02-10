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
    private final com.movie.theatre.security.JwtUtil jwtUtil;

    public ScreenController(ScreenService screenService, com.movie.theatre.security.JwtUtil jwtUtil) {
        this.screenService = screenService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Screen add(@Valid @RequestBody CreateScreenRequest request) {
        Screen screen = new Screen();
        screen.setName(request.getName());
        screen.setCapacity(request.getCapacity());

        return screenService.addScreen(request.getTheatreId(), screen);
    }

    // Get screen by ID (used for inter-service communication)
    @GetMapping("/{screenId}")
    public Screen getById(@PathVariable("screenId") Long screenId) {
        return screenService.getScreenById(screenId);
    }

    @GetMapping("/theatre/{theatreId}")
    public List<Screen> getByTheatre(@PathVariable("theatreId") Long theatreId) {
        return screenService.getScreensByTheatre(theatreId);
    }

    @PutMapping("/status")
    public Screen updateStatus(@Valid @RequestBody UpdateScreenStatusRequest request,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(token);
        Long userId = claims.get("userId", Long.class);
        List<String> roles = claims.get("roles", List.class);

        return screenService.updateStatus(
                request.getScreenId(),
                Screen.Status.valueOf(request.getStatus()),
                userId,
                roles);
    }

    // Public / Operational – ONLY ACTIVE
    @GetMapping("/active/theatre/{theatreId}")
    public List<Screen> getActiveByTheatre(@PathVariable("theatreId") Long theatreId) {
        return screenService.getActiveScreensByTheatre(theatreId);
    }

}
