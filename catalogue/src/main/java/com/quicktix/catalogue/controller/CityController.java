package com.quicktix.catalogue.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.catalogue.dto.ApiResponse;
import com.quicktix.catalogue.dto.CityResponseDTO;
import com.quicktix.catalogue.dto.CreateCityRequest;
import com.quicktix.catalogue.service.CityService;

@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // Public: city selection screen

    // --------------------------------------------------
    // GET ALL CITIES (Public)
    // --------------------------------------------------
    @GetMapping
    public List<CityResponseDTO> getAllCities() {
        return cityService.getAllCities();
    }

    // --------------------------------------------------
    // GET CITY BY ID (Public)
    // --------------------------------------------------
    @GetMapping("/{id}")
    public CityResponseDTO getCity(@PathVariable Integer id) {
        return cityService.getCityById(id);
    }

    // Admin
    // --------------------------------------------------
    // CREATE CITY (ADMIN)
    // --------------------------------------------------
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CityResponseDTO> createCity(
            @RequestBody CreateCityRequest request) {
        return new ApiResponse<>(true, "City created",
                cityService.createCity(request));
    }

    // --------------------------------------------------
    // DEACTIVATE CITY (ADMIN)
    // --------------------------------------------------
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateCity(@PathVariable Integer id) {
        cityService.deactivateCity(id);
        return new ApiResponse<>(true, "City deactivated", null);
    }

    // --------------------------------------------------
    // UPDATE CITY (ADMIN)
    // --------------------------------------------------
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CityResponseDTO> updateCity(
            @PathVariable Integer id,
            @RequestBody CreateCityRequest request) {
        return new ApiResponse<>(true, "City updated",
                cityService.updateCity(id, request));
    }

}