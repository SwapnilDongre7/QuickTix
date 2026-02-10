//package com.quicktix.catalogue.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.quicktix.catalogue.dto.ApiResponse;
//import com.quicktix.catalogue.dto.CreateMovieRequest;
//import com.quicktix.catalogue.dto.MovieFilter;
//import com.quicktix.catalogue.dto.MovieResponseDTO;
//import com.quicktix.catalogue.dto.UpdateMovieRequest;
//import com.quicktix.catalogue.exception.BadRequestException;
//import com.quicktix.catalogue.service.MovieService;
//
//import jakarta.validation.Validator;
//
//@RestController
//@RequestMapping("/movies")
//public class MovieController {
//


//    @Autowired
//    private Validator validator;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private final MovieService movieService;
//
//    public MovieController(MovieService movieService) {
//        this.movieService = movieService;
//    }
//
//    // --------------------------------------------------
//    // GET ALL MOVIES (Public)
//    // --------------------------------------------------
//    @GetMapping
//    public Page<MovieResponseDTO> getAllMovies(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) Integer genreId,
//            @RequestParam(required = false) Integer languageId) {
//        Pageable pageable = PageRequest.of(page, size);
//
//        MovieFilter filter = new MovieFilter();
//        filter.setGenreId(genreId);
//        filter.setLanguageId(languageId);
//
//        return movieService.getAllMovies(pageable, filter);
//    }
//
//    // --------------------------------------------------
//    // GET MOVIE BY ID (Public)
//    // --------------------------------------------------
//    @GetMapping("/{id}")
//    public MovieResponseDTO getMovieById(@PathVariable Long id) {
//        return movieService.getMovieById(id);
//    }
//
//    // --------------------------------------------------
//    // SEARCH MOVIES (Public)
//    // --------------------------------------------------
//    @GetMapping("/search")
//    public Page<MovieResponseDTO> searchMovies(
//            @RequestParam String query,
//            @PageableDefault(size = 10) Pageable pageable) {
//        return movieService.searchMovies(query, pageable);
//    }
//
//    // --------------------------------------------------
//    // GET UPCOMING MOVIES (Public)
//    // --------------------------------------------------
//    @GetMapping("/upcoming")
//    public Page<MovieResponseDTO> getUpcomingMovies(
//            @PageableDefault(size = 10, sort = "releaseDate") Pageable pageable) {
//        return movieService.getUpcomingMovies(pageable);
//    }
//
//    // --------------------------------------------------
//    // CREATE MOVIE (ADMIN)
//    // --------------------------------------------------
//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<MovieResponseDTO> createMovie(
//            @RequestPart("data") String data,
//            @RequestPart(value = "poster", required = false) MultipartFile poster,
//            @RequestPart(value = "background", required = false) MultipartFile background) throws Exception {
//
//        CreateMovieRequest request = objectMapper.readValue(data, CreateMovieRequest.class);
//
//        validator.validate(request).forEach(violation -> {
//            throw new BadRequestException(violation.getMessage());
//        });
//
//        MovieResponseDTO response = movieService.createMovie(request, poster, background);
//
//        return new ApiResponse<>(true, "Movie created successfully", response);
//    }
//
//    // --------------------------------------------------
//    // UPDATE MOVIE (ADMIN)
//    // --------------------------------------------------
//
//    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<MovieResponseDTO> updateMovie(
//            @PathVariable Long id,
//            @RequestPart("data") String data,
//            @RequestPart(value = "poster", required = false) MultipartFile poster,
//            @RequestPart(value = "background", required = false) MultipartFile background) throws Exception {
//
//        UpdateMovieRequest request = objectMapper.readValue(data, UpdateMovieRequest.class);
//
//        MovieResponseDTO response = movieService.updateMovie(id, request, poster, background);
//
//        return new ApiResponse<>(true, "Movie updated successfully", response);
//    }
//
//    // --------------------------------------------------
//    // DELETE MOVIE (ADMIN)
//    // --------------------------------------------------
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<Void> deleteMovie(@PathVariable Long id) {
//        movieService.deleteMovie(id);
//        return new ApiResponse<>(true, "Movie deleted successfully", null);
//    }
//
//    // --------------------------------------------------
//    // GET DELETED MOVIES (ADMIN)
//    // --------------------------------------------------
//    @GetMapping("/deleted")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<Page<MovieResponseDTO>> getDeletedMovies(Pageable pageable) {
//
//        Page<MovieResponseDTO> movies = movieService.getDeletedMovies(pageable);
//
//        return new ApiResponse<>(true, "Deleted movies fetched", movies);
//    }
//
//}



package com.quicktix.catalogue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktix.catalogue.dto.ApiResponse;
import com.quicktix.catalogue.dto.CreateMovieRequest;
import com.quicktix.catalogue.dto.MovieFilter;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.dto.UpdateMovieRequest;
import com.quicktix.catalogue.exception.BadRequestException;
import com.quicktix.catalogue.service.MovieService;

import jakarta.validation.Validator;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // --------------------------------------------------
    // GET ALL MOVIES (Public)
    // --------------------------------------------------
    @GetMapping
    public Page<MovieResponseDTO> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer languageId,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);

        MovieFilter filter = new MovieFilter();
        filter.setGenreId(genreId);
        filter.setLanguageId(languageId);

        // Parse status if provided
        if (status != null && !status.isEmpty()) {
            try {
                filter.setStatus(com.quicktix.catalogue.enums.MovieStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        return movieService.getAllMovies(pageable, filter);
    }

    // --------------------------------------------------
    // GET MOVIE BY ID (Public)
    // --------------------------------------------------
    @GetMapping("/{id}")
    public MovieResponseDTO getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    // --------------------------------------------------
    // SEARCH MOVIES (Public)
    // --------------------------------------------------
    @GetMapping("/search")
    public Page<MovieResponseDTO> searchMovies(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        return movieService.searchMovies(query, pageable);
    }

    // --------------------------------------------------
    // GET UPCOMING MOVIES (Public)
    // --------------------------------------------------
    @GetMapping("/upcoming")
    public Page<MovieResponseDTO> getUpcomingMovies(
            @PageableDefault(size = 10, sort = "releaseDate") Pageable pageable) {
        return movieService.getUpcomingMovies(pageable);
    }

    // --------------------------------------------------
    // CREATE MOVIE (ADMIN)
    // --------------------------------------------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MovieResponseDTO> createMovie(
            @RequestPart("data") String data,
            @RequestPart(value = "poster", required = false) MultipartFile poster,
            @RequestPart(value = "background", required = false) MultipartFile background) throws Exception {

        CreateMovieRequest request = objectMapper.readValue(data, CreateMovieRequest.class);

        validator.validate(request).forEach(violation -> {
            throw new BadRequestException(violation.getMessage());
        });

        MovieResponseDTO response = movieService.createMovie(request, poster, background);

        return new ApiResponse<>(true, "Movie created successfully", response);
    }

    // --------------------------------------------------
    // UPDATE MOVIE (ADMIN)
    // --------------------------------------------------

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MovieResponseDTO> updateMovie(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "poster", required = false) MultipartFile poster,
            @RequestPart(value = "background", required = false) MultipartFile background) throws Exception {

        UpdateMovieRequest request = objectMapper.readValue(data, UpdateMovieRequest.class);

        MovieResponseDTO response = movieService.updateMovie(id, request, poster, background);

        return new ApiResponse<>(true, "Movie updated successfully", response);
    }

    // --------------------------------------------------
    // DELETE MOVIE (ADMIN)
    // --------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return new ApiResponse<>(true, "Movie deleted successfully", null);
    }

    // --------------------------------------------------
    // GET DELETED MOVIES (ADMIN)
    // --------------------------------------------------
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<MovieResponseDTO>> getDeletedMovies(Pageable pageable) {

        Page<MovieResponseDTO> movies = movieService.getDeletedMovies(pageable);

        return new ApiResponse<>(true, "Deleted movies fetched", movies);
    }

}

