package com.quicktix.catalogue.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quicktix.catalogue.dto.CreateMovieRequest;
import com.quicktix.catalogue.dto.MovieFilter;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.dto.UpdateMovieRequest;
import com.quicktix.catalogue.entity.Genre;
import com.quicktix.catalogue.entity.Language;
import com.quicktix.catalogue.entity.Movie;
import com.quicktix.catalogue.enums.MovieStatus;
import com.quicktix.catalogue.exception.BadRequestException;
import com.quicktix.catalogue.exception.ResourceNotFoundException;
import com.quicktix.catalogue.repository.GenreRepository;
import com.quicktix.catalogue.repository.LanguageRepository;
import com.quicktix.catalogue.repository.MovieRepository;
import com.quicktix.catalogue.service.ImageUploadService;
import com.quicktix.catalogue.service.MovieService;

@Service
@Transactional
public class MovieServiceImpl implements MovieService {

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final LanguageRepository languageRepository;
	private final ImageUploadService imageUploadService;

	public MovieServiceImpl(MovieRepository movieRepository, GenreRepository genreRepository,
			LanguageRepository languageRepository, ImageUploadService imageUploadService) {
		super();
		this.movieRepository = movieRepository;
		this.genreRepository = genreRepository;
		this.languageRepository = languageRepository;
		this.imageUploadService = imageUploadService;
	}

	// --------------------------------------------------
    // GET ALL MOVIES (with pagination & filters)
    // --------------------------------------------------
	
	@Override
    public Page<MovieResponseDTO> getAllMovies(Pageable pageable, MovieFilter filter) {

        Page<Movie> movies;

        if (filter != null && filter.getGenreId() != null) {
        	// Filter by genre
            movies = movieRepository.findByGenre(filter.getGenreId(), pageable);
        } else if (filter != null && filter.getLanguageId() != null) {
        	// Filter by language
            movies = movieRepository.findByLanguage(filter.getLanguageId(), pageable);
        } else {
        	// No filters, get all movies
            movies = movieRepository.findAllActive(pageable);
        }

        return movies.map(this::mapToResponseDTO);
    }

	 // --------------------------------------------------
    // GET MOVIE BY ID
    // --------------------------------------------------
    @Override
    public MovieResponseDTO getMovieById(Long id) {

    	Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
    	        .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);
        return mapToResponseDTO(movie);
    }
	
	
	
    // --------------------------------------------------
    // CREATE MOVIE (ADMIN)
    // --------------------------------------------------
    @Override
    public MovieResponseDTO createMovie(CreateMovieRequest request, MultipartFile poster, MultipartFile background) {

    		// Basic validations
    	
			if (request.getTitle() == null || request.getTitle().isBlank()) {
				throw new BadRequestException("Title is required");
			}

			if (request.getReleaseDate() == null) {
				throw new BadRequestException("Release date is required");
			}

			if (request.getRating() == null) {
				throw new BadRequestException("Rating is required");
			}

			if (request.getRating().doubleValue() < 0 || request.getRating().doubleValue() > 5) {
				throw new BadRequestException("Rating must be between 0 and 5");
			}

			if (request.getAgeRating() == null) {
				throw new BadRequestException("Age rating is required");
			}

			if (request.getGenreIds() == null || request.getGenreIds().isEmpty()) {
				throw new BadRequestException("At least one genre is required");
			}

			if (request.getLanguageIds() == null || request.getLanguageIds().isEmpty()) {
				throw new BadRequestException("At least one language is required");
			}
    	
    	
    	
    	
        Movie movie = new Movie();

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setRating(request.getRating());
        movie.setAgeRating(request.getAgeRating());
        movie.setStatus(
        	    request.getStatus() != null
        	        ? request.getStatus()
        	        : MovieStatus.UPCOMING
        );
        movie.setViewCount(0L);

        
        
        
//        movie.setCastMembers(String.join(",", request.getCast()));
//        movie.setCrewMembers(String.join(",", request.getCrew()));
        
        if (request.getCast() != null) {
            movie.setCastMembers(String.join(",", request.getCast()));
        }

        if (request.getCrew() != null) {
            movie.setCrewMembers(String.join(",", request.getCrew()));
        }

        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
        if (genres.size() != request.getGenreIds().size()) {
            throw new ResourceNotFoundException("Invalid genre id");
        }
        movie.setGenres(genres);
        
        Set<Language> languages = new HashSet<>(languageRepository.findAllById(request.getLanguageIds()));
        if (languages.size() != request.getLanguageIds().size()) {
            throw new ResourceNotFoundException("Invalid language id");
        }
        movie.setLanguages(languages);
        
        
        // Image uploads
        if (poster != null && !poster.isEmpty()) {
            try {
                String posterUrl = imageUploadService.uploadImage(poster);
                movie.setPosterUrl(posterUrl); 
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed");
            }
        }

        
        if (background != null && !background.isEmpty()) {
            try {
                String backgroundUrl = imageUploadService.uploadImage(background);
                movie.setBackgroundUrl(backgroundUrl); 
            } catch (Exception e) {
                throw new RuntimeException("Image upload failed");
            }
        }
        
        

        Movie savedMovie = movieRepository.save(movie);
        movieRepository.flush();
        System.out.println("SAVED POSTER URL = " + savedMovie.getPosterUrl());
        System.out.println("SAVED BACKGROUND URL = " + savedMovie.getBackgroundUrl());
        return mapToResponseDTO(savedMovie);
    }
	
	
    // --------------------------------------------------
    // UPDATE MOVIE (ADMIN)
    // --------------------------------------------------
    
    @Override
    @Transactional
    public MovieResponseDTO updateMovie(
            Long id,
            UpdateMovieRequest request,
            MultipartFile poster,
            MultipartFile background) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        
        // Prevent updates to deleted movies
        if (movie.getIsDeleted()) {
            throw new IllegalStateException("Cannot update a deleted movie");
        }
        
        if (request.getTitle() != null)
            movie.setTitle(request.getTitle());

        if (request.getDescription() != null)
            movie.setDescription(request.getDescription());

        if (request.getDurationMinutes() != null)
            movie.setDurationMinutes(request.getDurationMinutes());

        if (request.getReleaseDate() != null)
            movie.setReleaseDate(request.getReleaseDate());

        if (request.getRating() != null)
            movie.setRating(request.getRating());

        if (request.getAgeRating() != null)
            movie.setAgeRating(request.getAgeRating());

        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }
        
        // Image updates
        if (poster != null && !poster.isEmpty()) {
            movie.setPosterUrl(imageUploadService.uploadImage(poster));
        }

        if (background != null && !background.isEmpty()) {
            movie.setBackgroundUrl(imageUploadService.uploadImage(background));
        }

        // Update genres
        if (request.getGenreIds() != null) {
        	Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
        	if (genres.size() != request.getGenreIds().size()) {
        	    throw new ResourceNotFoundException("Invalid genre id");
        	}
        	movie.setGenres(genres);
        }

        // Update languages
        if (request.getLanguageIds() != null) {
            Set<Language> languages =
                    new HashSet<>(languageRepository.findAllById(request.getLanguageIds()));
            if (languages.size() != request.getLanguageIds().size()) {
                throw new ResourceNotFoundException("Invalid language id");
            }
            movie.setLanguages(languages);
        }

        // Update cast & crew
        if (request.getCast() != null)
            movie.setCastMembers(String.join(",", request.getCast()));

        if (request.getCrew() != null)
            movie.setCrewMembers(String.join(",", request.getCrew()));

        return mapToResponseDTO(movieRepository.save(movie));
    }

	
	
    // --------------------------------------------------
    // DELETE MOVIE (ADMIN)
    // --------------------------------------------------
    @Override
    public void deleteMovie(Long id) {

    	Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Movie not found with id: " + id));

    	// Soft delete
        movie.setIsDeleted(true);

        movieRepository.save(movie);
    }
	
    
    // --------------------------------------------------
    // SEARCH MOVIES
    // --------------------------------------------------
    @Override
    public Page<MovieResponseDTO> searchMovies(String query, Pageable pageable) {

        // For now, only TITLE/DESCRIPTION search is implemented
    	Page<Movie> result =
                movieRepository.searchMovies(query, pageable);

        // If title/description search is empty → search cast/crew
        if (result.isEmpty()) {
            result = movieRepository.searchByCastOrCrew(query, pageable);
        }

        return result.map(this::mapToResponseDTO);
    }

    
    // --------------------------------------------------
    // FUTURE: SHOW SERVICE INTEGRATION
    // --------------------------------------------------
    @Override
    public Page<MovieResponseDTO> getNowShowingMovies(Integer cityId) {
        // Will be implemented via Show Service
        return Page.empty();
    }

    

	

    // --------------------------------------------------
    // VIEW COUNT
    // --------------------------------------------------
    @Override
    public void incrementViewCount(Long movieId) {
    	Movie movie = movieRepository
                .findByIdAndIsDeletedFalse(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Movie not found"));

        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);

    }
    
    
    // --------------------------------------------------
    // UPCOMING MOVIES
    // --------------------------------------------------
    @Override
    public Page<MovieResponseDTO> getUpcomingMovies(Pageable pageable) {

    	return movieRepository
    	        .findByStatusAndIsDeletedFalse(MovieStatus.UPCOMING, pageable)
    	        .map(this::mapToResponseDTO);
    }
    
    
 // --------------------------------------------------
 // ADMIN: UPDATE MOVIE STATUS
 // --------------------------------------------------
 @Override
 public void updateMovieStatus(Long movieId, MovieStatus status) {

     Movie movie = movieRepository.findByIdAndIsDeletedFalse(movieId)
             .orElseThrow(() ->
                     new ResourceNotFoundException("Movie not found with id: " + movieId));

     movie.setStatus(status);
     movieRepository.save(movie);
 }

    
    
    
    private MovieResponseDTO mapToResponseDTO(Movie movie) {

        MovieResponseDTO dto = new MovieResponseDTO();

        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setRating(movie.getRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setViewCount(movie.getViewCount());
        dto.setAgeRating(movie.getAgeRating());
        dto.setBackgroundUrl(movie.getBackgroundUrl());
        dto.setUpcoming(movie.getStatus() == MovieStatus.UPCOMING);
        
        if (movie.getCastMembers() != null && !movie.getCastMembers().isEmpty()) {
            dto.setCast(
                Arrays.asList(movie.getCastMembers().split(","))
            );
        }
        
        if (movie.getCrewMembers() != null && !movie.getCrewMembers().isEmpty()) {
            dto.setCrew(
                Arrays.asList(movie.getCrewMembers().split(","))
            );
        }

        if (movie.getGenres() != null) {
            dto.setGenres(
                    movie.getGenres().stream()
                            .map(Genre::getName)
                            .collect(Collectors.toSet())
            );
        }

        if (movie.getLanguages() != null) {
            dto.setLanguages(
                    movie.getLanguages().stream()
                            .map(Language::getName)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }

    @Override
    public Page<MovieResponseDTO> getDeletedMovies(Pageable pageable) {

        return movieRepository
                .findAllDeleted(pageable)
                .map(this::mapToResponseDTO);
    }

	
}
