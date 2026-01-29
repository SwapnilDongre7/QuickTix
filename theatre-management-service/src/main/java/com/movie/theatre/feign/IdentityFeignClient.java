package com.movie.theatre.feign;

import com.movie.theatre.feign.config.FeignConfig;
import com.movie.theatre.feign.fallback.IdentityFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Identity Service.
 * 
 * Provides declarative REST client for role management operations.
 * Uses Eureka service discovery via service name "IDENTITY-SERVICE".
 */
@FeignClient(name = "IDENTITY-SERVICE", configuration = FeignConfig.class, fallbackFactory = IdentityFeignClientFallbackFactory.class)
public interface IdentityFeignClient {

    /**
     * Add a role to a user.
     * Used when approving theatre owner applications.
     *
     * @param userId   User ID
     * @param roleName Role name to add (e.g., "THEATRE_OWNER")
     * @return Success message
     */
    @PutMapping("/auth/user/{userId}/role")
    String addRoleToUser(
            @PathVariable("userId") Long userId,
            @RequestParam("roleName") String roleName);
}
