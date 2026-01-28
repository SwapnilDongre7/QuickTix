package com.quicktix.catalogue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quicktix.catalogue.entity.Language;
import com.quicktix.catalogue.entity.Movie;

public interface LanguageRepository extends JpaRepository<Language, Integer> {

	Optional<Language> findByNameIgnoreCase(String name);

    Optional<Language> findByCodeIgnoreCase(String code);

    @Query("""
        SELECT m FROM Movie m
        JOIN m.languages l
        WHERE l.id = :languageId
    """)
    List<Movie> findMoviesByLanguage(@Param("languageId") Integer languageId);
}
