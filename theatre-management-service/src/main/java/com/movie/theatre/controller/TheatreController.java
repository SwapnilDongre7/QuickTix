package com.movie.theatre.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.theatre.dto.CreateTheatreRequest;
import com.movie.theatre.dto.UpdateTheatreStatusRequest;
import com.movie.theatre.entity.Theatre;
import com.movie.theatre.service.TheatreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }
    //it is used to create a new theatre
    @PostMapping
    public Theatre create(@Valid @RequestBody CreateTheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setCityId(request.getCityId());
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());

        return theatreService.createTheatre(request.getOwnerId(), theatre);
    }
    // Admin / Management – ALL theatres
    @GetMapping
    public List<Theatre> getAll() {
    	return theatreService.getAllTheatres();
    }
    
    // Public – ONLY ACTIVE theatres
    @GetMapping("/active")
    public List<Theatre> getAllActive() {
    	return theatreService.getAllActiveTheatres();
    }
    
    //it is used to get theatres by owner id
    @GetMapping("/owner/{ownerId}")
    public List<Theatre> getByOwner(@PathVariable("ownerId") Long ownerId) {
        return theatreService.getTheatresByOwner(ownerId);
    }
	//it is used to get theatres by city id
    @GetMapping("/city/{cityId}")
    public List<Theatre> getByCity(@PathVariable("cityId") Integer cityId) {
        return theatreService.getTheatresByCity(cityId);
    }
    //it is used to update the status of a theatre
    @PutMapping("/status")
    public Theatre updateStatus(@Valid @RequestBody UpdateTheatreStatusRequest request) {
        return theatreService.updateStatus(
                request.getTheatreId(),
                Theatre.Status.valueOf(request.getStatus())
        );
    }
    
 // Public / Operational – ONLY ACTIVE
    @GetMapping("/active/city/{cityId}")
    public List<Theatre> getActiveByCity(@PathVariable("cityId") Integer cityId) {
        return theatreService.getActiveTheatresByCity(cityId);
    }

    @GetMapping("/active/owner/{ownerId}")
    public List<Theatre> getActiveByOwner(@PathVariable("ownerId") Long ownerId) {
        return theatreService.getActiveTheatresByOwner(ownerId);
    }
    
    

}
