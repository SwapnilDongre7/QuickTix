package com.cdac.booking.feign;

import com.cdac.booking.feign.config.FeignConfig;
import com.cdac.booking.feign.dto.MovieResponseDto;
import com.cdac.booking.feign.fallback.CatalogueFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Catalogue Service.
 * 
 * Provides declarative REST client for movie-related operations.
 * Uses Eureka service discovery via service name "CATALOGUE-SERVICE".
 */
@FeignClient(name = "CATALOGUE-SERVICE", configuration = FeignConfig.class, fallbackFactory = CatalogueFeignClientFallbackFactory.class)
public interface CatalogueFeignClient {

    /**
     * Get movie details by ID.
     * Used for enriching ticket response with movie name.
     */
    @GetMapping("/movies/{movieId}")
    MovieResponseDto getMovieById(@PathVariable("movieId") Long movieId);
}
