package com.quicktix.catalogue.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    // GET GENRE BY ID (Public)
    // --------------------------------------------------
    @GetMapping("/{id}")
    public GenreDTO getGenre(@PathVariable Integer id) {
        return genreService.getGenreById(id);
    }

    // --------------------------------------------------
    // CREATE GENRE (ADMIN)
    // --------------------------------------------------
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GenreDTO> createGenre(@RequestParam String name) {
        GenreDTO genre = genreService.createGenre(name);
        return new ApiResponse<>(true, "Genre created successfully", genre);
    }

    // --------------------------------------------------
    // UPDATE GENRE (ADMIN)
    // --------------------------------------------------
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GenreDTO> updateGenre(
            @PathVariable Integer id,
            @RequestParam String name) {
        GenreDTO genre = genreService.updateGenre(id, name);
        return new ApiResponse<>(true, "Genre updated successfully", genre);
    }

    // --------------------------------------------------
    // DELETE GENRE (ADMIN)
    // --------------------------------------------------
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteGenre(@PathVariable Integer id) {
        genreService.deleteGenre(id);
        return new ApiResponse<>(true, "Genre deleted successfully", null);
    }

    // --------------------------------------------------
    // GET MOVIES BY GENRE (Public)
    // --------------------------------------------------
    @GetMapping("/{id}/movies")
    public List<MovieResponseDTO> getMoviesByGenre(@PathVariable Integer id) {
        return genreService.getMoviesByGenre(id);
    }
}