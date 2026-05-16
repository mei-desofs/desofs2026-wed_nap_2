package isep.desosfs.arcadehaven.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
public class SecurityConfig {

    private final SecurityEventHandler securityEventHandler;

    public SecurityConfig(SecurityEventHandler securityEventHandler) {
        this.securityEventHandler = securityEventHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/publisher/**").hasRole("PUBLISHER")
                        .requestMatchers("/api/orders/**").hasRole("BUYER")
                        .requestMatchers("/api/library/**").hasRole("BUYER")
                        .requestMatchers("/api/profile/**").authenticated()
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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
