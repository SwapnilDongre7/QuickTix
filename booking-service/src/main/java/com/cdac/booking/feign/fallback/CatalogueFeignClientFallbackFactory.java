package com.cdac.booking.feign.fallback;

import com.cdac.booking.feign.CatalogueFeignClient;
import com.cdac.booking.feign.dto.MovieResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for CatalogueFeignClient.
 * 
 * Provides graceful degradation when Catalogue service is unavailable.
 * Returns placeholder values instead of failing completely.
 */
@Slf4j
@Component
public class CatalogueFeignClientFallbackFactory implements FallbackFactory<CatalogueFeignClient> {

    @Override
    public CatalogueFeignClient create(Throwable cause) {
        log.error("Catalogue service fallback triggered: {}", cause.getMessage());

        return new CatalogueFeignClient() {
            @Override
            public MovieResponseDto getMovieById(Long movieId) {
                log.warn("Fallback: Unable to fetch movie {} from Catalogue service. Error: {}",
                        movieId, cause.getMessage());

                // Return placeholder data for graceful degradation
                MovieResponseDto fallback = new MovieResponseDto();
                fallback.setId(movieId);
                fallback.setTitle("Movie Unavailable");
                return fallback;
            }
        };
    }
}
