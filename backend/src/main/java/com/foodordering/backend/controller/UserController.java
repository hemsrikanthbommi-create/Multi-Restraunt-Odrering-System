package com.foodordering.backend.controller;

import com.foodordering.backend.entity.User;
import com.foodordering.backend.repository.UserRepository;
import com.foodordering.backend.security.JwtService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping("/register")
    public User register(
            @RequestBody User user) {

        if (user.getName() == null ||
                user.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Name is required");
        }

        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required");
        }

        if (user.getPassword() == null ||
                user.getPassword().trim().isEmpty()) {

            throw new RuntimeException(
                    "Password is required");
        }

        if (userRepository
                .findByEmail(user.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email already registered");
        }


        // Every newly registered user is a customer.
        // Users cannot choose their own role during
        // registration.
        user.setRole("CUSTOMER");


        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );


        return userRepository.save(user);
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody User loginUser) {

        User user = userRepository
                .findByEmail(
                        loginUser.getEmail()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        ));


        if (!passwordEncoder.matches(
                loginUser.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password");
        }


        // Protect against older users whose role
        // may be NULL.
        if (user.getRole() == null ||
                user.getRole().trim().isEmpty()) {

            user.setRole("CUSTOMER");

            userRepository.save(user);
        }


        String token =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                );


        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }


    // =====================================================
    // GET USER PROFILE
    // =====================================================

    @GetMapping("/{id}")
    public User getUser(
            @PathVariable Long id,
            Authentication authentication) {

        User authenticatedUser =
                getAuthenticatedUser(
                        authentication
                );


        // A normal user can only access
        // their own profile.
        if (authenticatedUser.getId().equals(id)) {

            return authenticatedUser;
        }


        // Allow privileged roles to access
        // other user records when needed.
        if (hasRole(
                authentication,
                "ADMIN")) {

            return userRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            ));
        }


        throw new RuntimeException(
                "Unauthorized user access"
        );
    }


    // =====================================================
    // GET AUTHENTICATED USER
    // =====================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }


    // =====================================================
    // ROLE CHECK
    // =====================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        if (authentication == null) {

            return false;
        }


        String requiredAuthority =
                "ROLE_" + role;


        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(
                        requiredAuthority::equals
                );
    }


    // =====================================================
    // LOGIN RESPONSE
    // =====================================================

    public static class LoginResponse {

        private String token;
        private Long userId;
        private String name;
        private String email;
        private String role;


        public LoginResponse(
                String token,
                Long userId,
                String name,
                String email,
                String role) {

            this.token = token;
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
        }


        public String getToken() {

            return token;
        }


        public Long getUserId() {

            return userId;
        }


        public String getName() {

            return name;
        }


        public String getEmail() {

            return email;
        }


        public String getRole() {

            return role;
        }
    }
}