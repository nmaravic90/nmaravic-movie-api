package com.nmaravic.movie.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String MATCH_PATTERN = "/movies/**";

    private final KeycloakRoleConverter keycloakRoleConverter;

    public SecurityConfig(KeycloakRoleConverter keycloakRoleConverter) {
        this.keycloakRoleConverter = keycloakRoleConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/movies/top-rated").permitAll()
                        .requestMatchers(HttpMethod.GET, MATCH_PATTERN).hasAnyRole(USER_ROLE, ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/movies/*/reviews").hasAnyRole(USER_ROLE, ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, MATCH_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, MATCH_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, MATCH_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, MATCH_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return jwtConverter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}