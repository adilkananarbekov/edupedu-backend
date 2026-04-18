package com.edupedu.app.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowed-origin-patterns:}")
    private String configuredAllowedOriginPatterns;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/register",
            "/api/v1/auth/**",
            "/api/v1/forgot-password",
            "/api/v1/reset-password",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/ws-chat",
            "/ws-chat/**",
            "/",
            "/index.html",
    };

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                   .csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth -> auth
                                                      .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                      .requestMatchers("/api/v1/auth/**").permitAll()
                                                      .requestMatchers(PUBLIC_URLS).permitAll()
                                                      
                                                      // Admin and University Admin only (CRUD operations)
                                                      .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "UNIVERSITY_ADMIN")
                                                      
                                                      // Teacher-level access (teachers + admins)
                                                      .requestMatchers("/api/v1/teacher/**").hasAnyRole("ADMIN", "UNIVERSITY_ADMIN", "TEACHER")
                                                      
                                                      // University-scoped reads (admin, uni admin, teachers)
                                                      .requestMatchers("/api/v1/university/**").hasAnyRole("ADMIN", "UNIVERSITY_ADMIN", "TEACHER")
                                                      
                                                      // All authenticated users
                                                      .anyRequest().authenticated())
                   .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                   .authenticationProvider(this.authenticationProvider)
                   .addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class)
                   .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOriginPatterns = Arrays.stream(
                        configuredAllowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        if (!allowedOriginPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
