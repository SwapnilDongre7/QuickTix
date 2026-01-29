package com.ticketbooking.identity.service;

import com.ticketbooking.identity.dto.*;
import com.ticketbooking.identity.entity.*;
import com.ticketbooking.identity.exception.InvalidCredentialsException;
import com.ticketbooking.identity.exception.RoleNotFoundException;
import com.ticketbooking.identity.exception.UserAlreadyExistsException;
import com.ticketbooking.identity.exception.UserNotFoundException;
import com.ticketbooking.identity.repository.*;
import com.ticketbooking.identity.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone());

        user = userRepository.save(user);

        // Always default to USER role - admin-controlled upgrades only
        Role.RoleName roleName = Role.RoleName.USER;
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setId(new UserRoleId(user.getId(), role.getId()));

        userRoleRepository.save(userRole);
    }

    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> roles = new ArrayList<>();
        user.getUserRoles().forEach(ur -> roles.add(ur.getRole().getRoleName().name()));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", user.getId());

        String token = jwtUtil.generateToken(user.getEmail(), claims);

        return new AuthResponse(token, user.getId(), roles);
    }

    public TokenValidationResponse validateToken(String token) {

        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> roles = new ArrayList<>();
        user.getUserRoles().forEach(ur -> roles.add(ur.getRole().getRoleName().name()));

        return new TokenValidationResponse(
                user.getId(),
                user.getEmail(),
                roles,
                true);
    }

    public void addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Role.RoleName roleEnum = Role.RoleName.valueOf(roleName);
        Role role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        boolean alreadyHasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleName() == roleEnum);

        if (alreadyHasRole) {
            return; // Idempotent
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setId(new UserRoleId(user.getId(), role.getId()));

        userRoleRepository.save(userRole);
    }
}
