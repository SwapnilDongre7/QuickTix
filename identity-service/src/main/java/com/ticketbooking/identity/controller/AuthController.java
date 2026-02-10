package com.ticketbooking.identity.controller;

import com.ticketbooking.identity.dto.*;
import com.ticketbooking.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * Note: All users are registered with USER role by default.
     * Role upgrades are admin-controlled via addRoleToUser endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        return ResponseEntity.ok(authService.validateToken(token));
    }

    /**
     * Add a role to a user.
     * This endpoint is ADMIN-only to prevent privilege escalation.
     * Used by Theatre Service after admin approves a theatre owner application.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{userId}/role")
    public ResponseEntity<String> addRoleToUser(@PathVariable Long userId, @RequestParam String roleName) {
        authService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok("Role added successfully");
    }

    /**
     * Get user info by ID.
     * Used by Theatre Service to display user details in pending applications.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

}
