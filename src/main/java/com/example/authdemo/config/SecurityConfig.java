package com.example.authdemo.config;



import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;



@Configuration

@EnableWebSecurity

public class SecurityConfig {



    @Bean

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(authz -> authz

                                .requestMatchers(

                                        "/api/auth/**",

                                        "/api/parcels/track/**",

                                        "/ws/**"

                                ).permitAll()

// ✅ FIX: Specific rules MUST come before the general rules.

                                .requestMatchers("/api/feedback/submit").authenticated()

                                .requestMatchers("/admin/feedback").hasRole("ADMIN")

                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

// ✅ FIX: This rule must always be the LAST one.

                                .anyRequest().authenticated()

                )

                .httpBasic();



        return http.build();

    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // IMPORTANT: Replace the localhost URL with "*" to allow all origins for now.
        // For production, you would list your specific frontend URL.
        configuration.setAllowedOrigins(Arrays.asList("*")); // Changed from localhost
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}

}