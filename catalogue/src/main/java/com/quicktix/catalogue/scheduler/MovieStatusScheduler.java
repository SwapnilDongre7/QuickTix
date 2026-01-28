package com.quicktix.catalogue.scheduler;


import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.catalogue.entity.Movie;
import com.quicktix.catalogue.enums.MovieStatus;
import com.quicktix.catalogue.repository.MovieRepository;

@Component
@Transactional
public class MovieStatusScheduler {

    private final MovieRepository movieRepository;

    public MovieStatusScheduler(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Runs every 1 minute
    @Scheduled(fixedDelay = 60000)
    public void updateMovieStatuses() {

        LocalDate today = LocalDate.now();

        List<Movie> movies = movieRepository.findAll();

        for (Movie movie : movies) {

        	if (Boolean.TRUE.equals(movie.getIsDeleted())) continue;

            if (movie.getReleaseDate() == null) continue;

            if (movie.getReleaseDate().isAfter(today)) {
                movie.setStatus(MovieStatus.UPCOMING);
            }
            else if (!movie.getReleaseDate().isAfter(today)) {
                movie.setStatus(MovieStatus.RUNNING);
            }

            // OPTIONAL future logic:
            // if movie has no shows → ENDED
        }

        movieRepository.saveAll(movies);
    }
}

