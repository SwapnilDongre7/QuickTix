package com.quicktix.catalogue.service;

import java.util.List;

import com.quicktix.catalogue.dto.GenreDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;

public interface GenreService {
	
	List<GenreDTO> getAllGenres();

    GenreDTO createGenre(String genre);

    List<MovieResponseDTO> getMoviesByGenre(Integer genreId);
}
