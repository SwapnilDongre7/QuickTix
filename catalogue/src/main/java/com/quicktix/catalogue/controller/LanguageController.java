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
    // GET LANGUAGE BY ID (Public)
    // --------------------------------------------------
    @GetMapping("/{id}")
    public LanguageDTO getLanguage(@PathVariable Integer id) {
        return languageService.getLanguageById(id);
    }

    // --------------------------------------------------
    // CREATE LANGUAGE (ADMIN)
    // --------------------------------------------------
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LanguageDTO> createLanguage(
            @RequestParam String name,
            @RequestParam String code) {
        LanguageDTO language = languageService.createLanguage(name, code);
        return new ApiResponse<>(true, "Language created successfully", language);
    }

    // --------------------------------------------------
    // UPDATE LANGUAGE (ADMIN)
    // --------------------------------------------------
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LanguageDTO> updateLanguage(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam String code) {
        LanguageDTO language = languageService.updateLanguage(id, name, code);
        return new ApiResponse<>(true, "Language updated successfully", language);
    }

    // --------------------------------------------------
    // DELETE LANGUAGE (ADMIN)
    // --------------------------------------------------
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteLanguage(@PathVariable Integer id) {
        languageService.deleteLanguage(id);
        return new ApiResponse<>(true, "Language deleted successfully", null);
    }

    // --------------------------------------------------
    // GET MOVIES BY LANGUAGE (Public)
    // --------------------------------------------------
    @GetMapping("/{id}/movies")
    public List<MovieResponseDTO> getMoviesByLanguage(@PathVariable Integer id) {
        return languageService.getMoviesByLanguage(id);
    }
}
