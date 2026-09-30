package com.foodordering.user_service.config;

import com.foodordering.user_service.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // Disable CSRF because this backend uses JWT
            // authentication instead of browser sessions.
            .csrf(AbstractHttpConfigurer::disable)

            // Disable default browser login page.
            .formLogin(AbstractHttpConfigurer::disable)

            // Disable HTTP Basic authentication.
            .httpBasic(AbstractHttpConfigurer::disable)

            // Enable CORS.
            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers(
                        "/api/users/login",
                        "/api/users/register",
                        "/api/health"
                ).permitAll()

                // Customer endpoints
                .requestMatchers(
                        "/api/cart/**",
                        "/api/orders/**",
                        "/api/order-items/**"
                ).hasRole("CUSTOMER")

                // Restaurant management
                .requestMatchers(
                        "/api/restaurant-orders/restaurant/**",
                        "/api/restaurant-orders/*/status"
                ).hasRole("RESTAURANT")

                // Delivery APIs
                .requestMatchers(
                        "/api/deliveries/**"
                ).hasAnyRole(
                        "CUSTOMER",
                        "DELIVERY_PARTNER"
                )

                // Delivery partner management
                .requestMatchers(
                        "/api/delivery-partners/**"
                ).hasRole("DELIVERY_PARTNER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}