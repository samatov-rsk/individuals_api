package org.samatov.individuals_api.config;

import org.samatov.individuals_api.config.converter.KeycloakRealmRoleConverter;
import org.samatov.individuals_api.config.converter.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/webjars/swagger-ui/index.html",
                                "/webjars/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/v3/api-docs.json",
                                "/v3/api-docs")
                        .permitAll()
                        .pathMatchers("/v1/auth/registration",
                                "/v1/auth/login",
                                "/v1/auth/refresh-token")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        new ReactiveJwtGrantedAuthoritiesConverterAdapter(
                                                new KeycloakRealmRoleConverter())))
                );
        return http.build();
    }
}
