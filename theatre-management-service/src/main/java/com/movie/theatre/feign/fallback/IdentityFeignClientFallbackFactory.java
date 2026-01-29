package com.movie.theatre.feign.fallback;

import com.movie.theatre.feign.IdentityFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for IdentityFeignClient.
 * 
 * Handles failures when Identity service is unavailable.
 * Role assignment failures are critical for theatre owner approval workflow.
 */
@Slf4j
@Component
public class IdentityFeignClientFallbackFactory implements FallbackFactory<IdentityFeignClient> {

    @Override
    public IdentityFeignClient create(Throwable cause) {
        log.error("Identity service fallback triggered: {}", cause.getMessage());

        return new IdentityFeignClient() {

            @Override
            public String addRoleToUser(Long userId, String roleName) {
                log.error("Fallback: Unable to add role {} to user {}. Error: {}",
                        roleName, userId, cause.getMessage());
                throw new RuntimeException(
                        "Failed to update user role in Identity Service: " + cause.getMessage());
            }
        };
    }
}
