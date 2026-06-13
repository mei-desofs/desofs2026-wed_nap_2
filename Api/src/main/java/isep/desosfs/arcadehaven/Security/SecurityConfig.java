package isep.desosfs.arcadehaven.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
public class SecurityConfig {

    private final SecurityEventHandler securityEventHandler;

    @Value("${security.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    public SecurityConfig(SecurityEventHandler securityEventHandler) {
        this.securityEventHandler = securityEventHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h
                        // HSTS max-age >= 1 year; sent on all requests (proxy terminates TLS)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000)
                                .requestMatcher(request -> true)
                        )
                        // Prevent MIME-type sniffing
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(Customizer.withDefaults())
                        // Prevent caching of sensitive responses in browsers and intermediaries
                        .cacheControl(Customizer.withDefaults())
                )
                // CORS restricted to explicit origin allowlist
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ASVS V13.4.4 — deny HTTP TRACE on all paths (prevents Cross-Site Tracing)
                        .requestMatchers(HttpMethod.TRACE, "/**").denyAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/publisher/**").hasRole("PUBLISHER")
                        .requestMatchers("/api/orders/**").hasRole("BUYER")
                        .requestMatchers("/api/library/**").hasRole("BUYER")
                        .requestMatchers("/api/profile/**").authenticated()
                        // ASVS V13.4.5 — API docs require authentication (not public in production)
                        .requestMatchers("/v3/api-docs/**").authenticated()
                        .requestMatchers("/swagger-ui/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(securityEventHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityEventHandler)
                        .accessDeniedHandler(securityEventHandler)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
