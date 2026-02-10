package com.quicktix.catalogue.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.catalogue.dto.GenreDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.entity.Genre;
import com.quicktix.catalogue.entity.Movie;
import com.quicktix.catalogue.exception.DuplicateResourceException;
import com.quicktix.catalogue.exception.ResourceNotFoundException;
import com.quicktix.catalogue.repository.GenreRepository;
import com.quicktix.catalogue.service.GenreService;

@Service
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    // --------------------------------------------------
    // GET ALL GENRES
    // --------------------------------------------------
    @Override
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(this::mapToGenreDTO)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------
    // GET GENRE BY ID
    // --------------------------------------------------
    @Override
    public GenreDTO getGenreById(Integer id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found"));
        return mapToGenreDTO(genre);
    }

    // --------------------------------------------------
    // CREATE GENRE (ADMIN)
    // --------------------------------------------------
    @Override
    public GenreDTO createGenre(String genreName) {

        genreRepository.findByNameIgnoreCase(genreName)
                .ifPresent(g -> {
                    throw new DuplicateResourceException("Genre already exists: " + genreName);
                });

        Genre genre = new Genre();
        genre.setName(genreName);

        Genre savedGenre = genreRepository.save(genre);

        return mapToGenreDTO(savedGenre);
    }

    // --------------------------------------------------
    // UPDATE GENRE (ADMIN)
    // --------------------------------------------------
    @Override
    public GenreDTO updateGenre(Integer id, String genreName) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found"));

        genreRepository.findByNameIgnoreCase(genreName)
                .ifPresent(g -> {
                    if (!g.getId().equals(id)) {
                        throw new DuplicateResourceException("Genre already exists: " + genreName);
                    }
                });

        genre.setName(genreName);
        return mapToGenreDTO(genreRepository.save(genre));
    }

    // --------------------------------------------------
    // DELETE GENRE (ADMIN)
    // --------------------------------------------------
    @Override
    public void deleteGenre(Integer id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre not found");
        }
        genreRepository.deleteById(id);
    }

    // --------------------------------------------------
    // GET MOVIES BY GENRE
    // --------------------------------------------------
    @Override
    public List<MovieResponseDTO> getMoviesByGenre(Integer genreId) {

        genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + genreId));

        List<Movie> movies = genreRepository.findMoviesByGenre(genreId);

        return movies.stream()
                .map(this::mapToMovieResponseDTO)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------
    // MAPPERS
    // --------------------------------------------------
    private GenreDTO mapToGenreDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }

    private MovieResponseDTO mapToMovieResponseDTO(Movie movie) {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setRating(movie.getRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setViewCount(movie.getViewCount());

        if (movie.getGenres() != null) {
            dto.setGenres(
                    movie.getGenres().stream()
                            .map(g -> g.getName())
                            .collect(Collectors.toSet()));
        }

        if (movie.getLanguages() != null) {
            dto.setLanguages(
                    movie.getLanguages().stream()
                            .map(l -> l.getName())
                            .collect(Collectors.toSet()));
        }

        return dto;
    }

}
