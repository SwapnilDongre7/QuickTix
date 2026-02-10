package com.quicktix.catalogue.service;

import java.util.List;

import com.quicktix.catalogue.dto.LanguageDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;

public interface LanguageService {

    List<LanguageDTO> getAllLanguages();

    LanguageDTO createLanguage(String language, String code);

    LanguageDTO getLanguageById(Integer id);

    LanguageDTO updateLanguage(Integer id, String name, String code);

    void deleteLanguage(Integer id);

    List<MovieResponseDTO> getMoviesByLanguage(Integer languageId);
}
