package com.quicktix.catalogue.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.catalogue.dto.ApiResponse;
import com.quicktix.catalogue.dto.GenreDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.service.GenreService;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    // --------------------------------------------------
    // GET ALL GENRES (Public)
    // --------------------------------------------------
    @GetMapping
    public List<GenreDTO> getAllGenres() {
        return genreService.getAllGenres();
    }

    // --------------------------------------------------
    // CREATE GENRE (ADMIN)
    // --------------------------------------------------
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GenreDTO> createGenre(@RequestParam String name) {
        GenreDTO genre = genreService.createGenre(name);
        return new ApiResponse<>(true, "Genre created successfully", genre);
    }

    // --------------------------------------------------
    // GET MOVIES BY GENRE (Public)
    // --------------------------------------------------
    @GetMapping("/{id}/movies")
    public List<MovieResponseDTO> getMoviesByGenre(@PathVariable Integer id) {
        return genreService.getMoviesByGenre(id);
    }
}