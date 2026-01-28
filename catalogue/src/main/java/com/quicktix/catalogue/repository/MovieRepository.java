package com.quicktix.catalogue.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quicktix.catalogue.entity.Movie;
import com.quicktix.catalogue.enums.AgeRating;
import com.quicktix.catalogue.enums.MovieStatus;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	// Pagination (GET /movies)
	//Page<Movie> findAll(Pageable pageable);
	
	@Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.isDeleted = false
    	""")
    	Page<Movie> findAllActive(Pageable pageable);

    // Search movies by title or description
    @Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.isDeleted = false
    	      AND (
    	            LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
    	         OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%'))
    	      )
    	""")
    	Page<Movie> searchMovies(@Param("query") String query, Pageable pageable);

    // Filter by genre
    @Query("""
    	    SELECT m FROM Movie m
    	    JOIN m.genres g
    	    WHERE g.id = :genreId
    	      AND m.isDeleted = false
    	""")
    	Page<Movie> findByGenre(@Param("genreId") Integer genreId, Pageable pageable);

    // Filter by language
    @Query("""
    	    SELECT m FROM Movie m
    	    JOIN m.languages l
    	    WHERE l.id = :languageId
    	      AND m.isDeleted = false
    	""")
    	Page<Movie> findByLanguage(@Param("languageId") Integer languageId, Pageable pageable);

    // Increment view count
    @Query("""
    	    UPDATE Movie m
    	    SET m.viewCount = m.viewCount + 1
    	    WHERE m.id = :movieId
    	      AND m.isDeleted = false
    	""")
    	void incrementViewCount(@Param("movieId") Long movieId);


    // Upcoming movies (releaseDate > today)
    Page<Movie> findByReleaseDateAfterAndIsDeletedFalse(
            LocalDate date,
            Pageable pageable
    );

    
    // Now showing movies (releaseDate <= today)
    Page<Movie> findByReleaseDateLessThanEqualAndIsDeletedFalse(
            LocalDate date,
            Pageable pageable
    );

    
    // Filter by age rating
    Page<Movie> findByAgeRatingAndIsDeletedFalse(
            AgeRating ageRating,
            Pageable pageable
    );

    
    @Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.isDeleted = false
    	      AND (
    	           LOWER(m.castMembers) LIKE LOWER(CONCAT('%', :name, '%'))
    	        OR LOWER(m.crewMembers) LIKE LOWER(CONCAT('%', :name, '%'))
    	      )
    	""")
    	Page<Movie> searchByCastOrCrew(
    	        @Param("name") String name,
    	        Pageable pageable
    	);

    	
    Optional<Movie> findByIdAndIsDeletedFalse(Long id);
    Page<Movie> findByIsDeletedTrue(Pageable pageable);


    Page<Movie> findByStatusAndIsDeletedFalse(
            MovieStatus status,
            Pageable pageable
    );

    
    @Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.isDeleted = true
    	""")
    	Page<Movie> findAllDeleted(Pageable pageable);

    
    // Find movies that need to be started (status UPCOMING and releaseDate <= today)
    @Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.status = 'UPCOMING'
    	      AND m.releaseDate <= CURRENT_DATE
    	      AND m.isDeleted = false
    	""")
    	List<Movie> findMoviesToStart();

    // Find movies that need to be ended (status RUNNING and endDate < today)
    @Query("""
    	    SELECT m FROM Movie m
    	    WHERE m.status = 'RUNNING'
    	      AND m.isDeleted = false
    	""")
    	List<Movie> findRunningMovies();

    // Update movie statuses in bulk
    @Modifying
    @Query("""
        UPDATE Movie m
        SET m.status = 'RUNNING'
        WHERE m.status = 'UPCOMING'
          AND m.releaseDate <= :today
          AND m.isDeleted = false
    """)
    void updateUpcomingToRunning(@Param("today") LocalDate today);

    
    // Update RUNNING movies to ENDED if their end date has passed
    @Modifying
    @Query("""
        UPDATE Movie m
        SET m.status = 'ENDED'
        WHERE m.status = 'RUNNING'
          AND m.releaseDate < :today
          AND m.isDeleted = false
    """)
    void updateRunningToEnded(@Param("today") LocalDate today);

    
}
