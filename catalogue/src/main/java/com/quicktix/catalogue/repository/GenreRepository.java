package com.quicktix.catalogue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quicktix.catalogue.entity.Genre;
import com.quicktix.catalogue.entity.Movie;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

	Optional<Genre> findByNameIgnoreCase(String name);

    @Query("""
        SELECT m FROM Movie m
        JOIN m.genres g
        WHERE g.id = :genreId
    """)
    List<Movie> findMoviesByGenre(@Param("genreId") Integer genreId);
}
