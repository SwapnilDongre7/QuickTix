package com.quicktix.catalogue.service;

import java.util.List;

import com.quicktix.catalogue.dto.GenreDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;

public interface GenreService {

    List<GenreDTO> getAllGenres();

    GenreDTO createGenre(String genre);

    GenreDTO getGenreById(Integer id);

    GenreDTO updateGenre(Integer id, String name);

    void deleteGenre(Integer id);

    List<MovieResponseDTO> getMoviesByGenre(Integer genreId);
}
