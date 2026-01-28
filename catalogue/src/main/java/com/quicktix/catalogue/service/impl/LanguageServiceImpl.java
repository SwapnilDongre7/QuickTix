package com.quicktix.catalogue.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.catalogue.dto.LanguageDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.entity.Language;
import com.quicktix.catalogue.entity.Movie;
import com.quicktix.catalogue.exception.BadRequestException;
import com.quicktix.catalogue.exception.DuplicateResourceException;
import com.quicktix.catalogue.exception.ResourceNotFoundException;
import com.quicktix.catalogue.repository.LanguageRepository;
import com.quicktix.catalogue.service.LanguageService;

@Service
@Transactional
public class LanguageServiceImpl implements LanguageService {

	private final LanguageRepository languageRepository;

    public LanguageServiceImpl(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    // --------------------------------------------------
    // GET ALL LANGUAGES
    // --------------------------------------------------
    @Override
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAll()
                .stream()
                .map(this::mapToLanguageDTO)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------
    // CREATE LANGUAGE (ADMIN)
    // --------------------------------------------------
    @Override
    public LanguageDTO createLanguage(String languageName, String code) {

    	if (languageName == null || languageName.isBlank()) {
            throw new BadRequestException("Language name is required");
        }

        if (code == null || code.isBlank()) {
            throw new BadRequestException("Language code is required");
        }
    	
        languageRepository.findByNameIgnoreCase(languageName)
                .ifPresent(l -> {
                    throw new DuplicateResourceException("Language already exists: " + languageName);
                });

        Language language = new Language();
        language.setName(languageName);
        language.setCode(code);

        Language savedLanguage = languageRepository.save(language);

        return mapToLanguageDTO(savedLanguage);
    }

    // --------------------------------------------------
    // GET MOVIES BY LANGUAGE
    // --------------------------------------------------
    @Override
    public List<MovieResponseDTO> getMoviesByLanguage(Integer languageId) {

        //Language language = languageRepository.findById(languageId)
        languageRepository.findById(languageId)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + languageId));

        List<Movie> movies = languageRepository.findMoviesByLanguage(languageId);

        return movies.stream()
                .map(this::mapToMovieResponseDTO)
                .collect(Collectors.toList());
    }
	
	// --------------------------------------------------
    // MAPPERS
    // --------------------------------------------------
    private LanguageDTO mapToLanguageDTO(Language language) {
        LanguageDTO dto = new LanguageDTO();
        dto.setId(language.getId());
        dto.setName(language.getName());
        dto.setCode(language.getCode());
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
                    .collect(Collectors.toSet())
            );
        }

        if (movie.getLanguages() != null) {
            dto.setLanguages(
                movie.getLanguages().stream()
                    .map(l -> l.getName())
                    .collect(Collectors.toSet())
            );
        }

        return dto;
    }

}
