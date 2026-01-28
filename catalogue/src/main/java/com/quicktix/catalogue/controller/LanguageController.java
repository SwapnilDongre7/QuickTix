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
import com.quicktix.catalogue.dto.LanguageDTO;
import com.quicktix.catalogue.dto.MovieResponseDTO;
import com.quicktix.catalogue.service.LanguageService;

@RestController
@RequestMapping("/languages")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    // --------------------------------------------------
    // GET ALL LANGUAGES (Public)
    // --------------------------------------------------
    @GetMapping
    public List<LanguageDTO> getAllLanguages() {
        return languageService.getAllLanguages();
    }

    // --------------------------------------------------
    // CREATE LANGUAGE (ADMIN)
    // --------------------------------------------------
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LanguageDTO> createLanguage(
            @RequestParam String name,
            @RequestParam String code
    ) {
        LanguageDTO language = languageService.createLanguage(name, code);
        return new ApiResponse<>(true, "Language created successfully", language);
    }

    // --------------------------------------------------
    // GET MOVIES BY LANGUAGE (Public)
    // --------------------------------------------------
    @GetMapping("/{id}/movies")
    public List<MovieResponseDTO> getMoviesByLanguage(@PathVariable Integer id) {
        return languageService.getMoviesByLanguage(id);
    }
}
