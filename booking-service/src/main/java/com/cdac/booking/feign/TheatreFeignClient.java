package com.cdac.booking.feign;

import com.cdac.booking.feign.config.FeignConfig;
import com.cdac.booking.feign.dto.ScreenResponseDto;
import com.cdac.booking.feign.dto.TheatreResponseDto;
import com.cdac.booking.feign.fallback.TheatreFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Theatre Service.
 * 
 * Provides declarative REST client for theatre and screen operations.
 * Uses Eureka service discovery via service name "THEATRE-SERVICE".
 */
@FeignClient(name = "THEATRE-SERVICE", configuration = FeignConfig.class, fallbackFactory = TheatreFeignClientFallbackFactory.class)
public interface TheatreFeignClient {

    /**
     * Get theatre details by ID.
     * Used for enriching ticket response with theatre name.
     */
    @GetMapping("/theatres/{theatreId}")
    TheatreResponseDto getTheatreById(@PathVariable("theatreId") Long theatreId);

    /**
     * Get screen details by ID.
     * Used for enriching ticket response with screen name.
     */
    @GetMapping("/screens/{screenId}")
    ScreenResponseDto getScreenById(@PathVariable("screenId") Long screenId);
}
