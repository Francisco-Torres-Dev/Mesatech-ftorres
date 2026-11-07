package com.mesatech.cloud.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String EXPECTED_AUDIENCE = "964cf08d-0742-47e1-bdf7-1b912dc8b506";
    private static final String EXPECTED_APPLICATION_ID_URI = "api://964cf08d-0742-47e1-bdf7-1b912dc8b506";
    private static final String REQUIRED_SCOPE = "SCOPE_access_as_user";
        private static final List<String> EXPECTED_ISSUERS = List.of(
            "https://login.microsoftonline.com/2b28c657-3169-48ee-91b9-d36cede4fe70/v2.0",
            "https://sts.windows.net/2b28c657-3169-48ee-91b9-d36cede4fe70/"
        );

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtClaimValidator<>(
            "iss",
            EXPECTED_ISSUERS::contains
        );
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                "aud",
            audience -> audience != null
                && (audience.contains(EXPECTED_AUDIENCE)
                || audience.contains(EXPECTED_APPLICATION_ID_URI))
        );
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            timestampValidator,
            issuerValidator,
            audienceValidator
        ));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "https://task-interstate-topic-optimal.trycloudflare.com",
    "https://members-introductory-programmer-see.trycloudflare.com"
));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/v1/auth/local/**").permitAll()
                .requestMatchers("/v1/catalogo/**", "/v1/solicitudes/**", "/v2/**").hasAuthority(REQUIRED_SCOPE)
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }
}

