package com.cdac.booking.feign.fallback;

import com.cdac.booking.feign.TheatreFeignClient;
import com.cdac.booking.feign.dto.ScreenResponseDto;
import com.cdac.booking.feign.dto.TheatreResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for TheatreFeignClient.
 * 
 * Provides graceful degradation when Theatre service is unavailable.
 * Returns placeholder values instead of failing completely.
 */
@Slf4j
@Component
public class TheatreFeignClientFallbackFactory implements FallbackFactory<TheatreFeignClient> {

    @Override
    public TheatreFeignClient create(Throwable cause) {
        log.error("Theatre service fallback triggered: {}", cause.getMessage());

        return new TheatreFeignClient() {
            @Override
            public TheatreResponseDto getTheatreById(Long theatreId) {
                log.warn("Fallback: Unable to fetch theatre {} from Theatre service. Error: {}",
                        theatreId, cause.getMessage());

                // Return placeholder data for graceful degradation
                TheatreResponseDto fallback = new TheatreResponseDto();
                fallback.setTheatreId(theatreId);
                fallback.setName("Theatre Unavailable");
                fallback.setAddress("N/A");
                return fallback;
            }

            @Override
            public ScreenResponseDto getScreenById(Long screenId) {
                log.warn("Fallback: Unable to fetch screen {} from Theatre service. Error: {}",
                        screenId, cause.getMessage());

                // Return placeholder data for graceful degradation
                ScreenResponseDto fallback = new ScreenResponseDto();
                fallback.setScreenId(screenId);
                fallback.setName("Screen Unavailable");
                return fallback;
            }
        };
    }
}
