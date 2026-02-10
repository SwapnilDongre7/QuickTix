package com.quicktix.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.quicktix.catalogue.dto.CreateMovieRequest;
import com.quicktix.catalogue.dto.MovieFilter;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.dto.UpdateMovieRequest;
import com.quicktix.catalogue.enums.MovieStatus;

public interface MovieService {

	Page<MovieResponseDTO> getAllMovies(Pageable pageable, MovieFilter filter);

	MovieResponseDTO getMovieById(Long id);

	MovieResponseDTO createMovie(CreateMovieRequest request, MultipartFile poster, MultipartFile background);

	MovieResponseDTO updateMovie(Long id, UpdateMovieRequest request, MultipartFile poster, MultipartFile background);

	void deleteMovie(Long id);

	Page<MovieResponseDTO> searchMovies(String query, Pageable pageable);

	Page<MovieResponseDTO> getDeletedMovies(Pageable pageable);
	
	void updateMovieStatus(Long movieId, MovieStatus status);

	
	// ---- FUTURE INTEGRATION (Show Service) ----
	Page<MovieResponseDTO> getNowShowingMovies(Integer cityId);

	Page<MovieResponseDTO> getUpcomingMovies(Pageable pageable);


	
	void incrementViewCount(Long movieId);
}
