package org.samatov.individuals_api.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class ReactiveJwtGrantedAuthoritiesConverterAdapter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    private final Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter;

    public ReactiveJwtGrantedAuthoritiesConverterAdapter(Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter) {
        this.grantedAuthoritiesConverter = grantedAuthoritiesConverter;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
}
