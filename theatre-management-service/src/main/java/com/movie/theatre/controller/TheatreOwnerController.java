package com.movie.theatre.controller;

import com.movie.theatre.dto.RegisterOwnerRequest;
import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.service.TheatreOwnerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owners")
public class TheatreOwnerController {

    private final TheatreOwnerService theatreOwnerService;

    public TheatreOwnerController(TheatreOwnerService theatreOwnerService) {
        this.theatreOwnerService = theatreOwnerService;
    }

    @PostMapping("/register")
    public TheatreOwner register(@Valid @RequestBody RegisterOwnerRequest request) {
        return theatreOwnerService.registerOwner(request.getUserId());
    }

    @PutMapping("/approve/{ownerId}")
    public TheatreOwner approve(@PathVariable("ownerId") Long ownerId) {
        return theatreOwnerService.approveOwner(ownerId);
    }
}
