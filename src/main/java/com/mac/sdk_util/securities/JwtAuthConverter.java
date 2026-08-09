package com.mac.sdk_util.securities;

import com.mac.sdk_util.entities.constant.JwtPayload;
import com.mac.sdk_util.config.securities.properties.JwtAuthConverterProperties;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthConverterProperties properties;

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    public Collection<GrantedAuthority> extractAuthorities(@NonNull Jwt jwt) {
        Collection<? extends GrantedAuthority> roles = extractRoles(jwt);
        Collection<? extends GrantedAuthority> permissions = extractPermissions(jwt);
        return Stream.of(
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                        roles.stream(),
                        permissions.stream())
                .flatMap(stream -> stream)
                .collect(Collectors.toSet());
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        log.debug("Building JwtAuthenticationToken");

        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        if (log.isDebugEnabled()) {
            log.debug("JWT authority mapping completed: {} authorities", authorities.size());
        }

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleClaimName(jwt));
    }

    public String getToken(Jwt jwt) {
        return jwt.getTokenValue();
    }

    private String getPrincipleClaimName(Jwt jwt) {
        log.debug("Resolving principal claim name");
        String claimName = JwtClaimNames.SUB;
        if (properties.getPrincipleAttribute() != null && !properties.getPrincipleAttribute().isEmpty()) {
            claimName = properties.getPrincipleAttribute();
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractRoles(Jwt source) {
        Set<SimpleGrantedAuthority> direct = mapRolesCollection(source.getClaim(JwtPayload.ROLES.getClaimKey()));
        Set<SimpleGrantedAuthority> realm = mapRolesFromNestedClaim(source, JwtPayload.REALM_ACCESS.getClaimKey());
        Set<SimpleGrantedAuthority> client =
                StringUtils.hasText(properties.getResourceId())
                        ? mapRolesFromResourceClient(source, properties.getResourceId().trim())
                        : Set.of();
        return Stream.of(direct.stream(), realm.stream(), client.stream())
                .flatMap(stream -> stream)
                .collect(Collectors.toSet());
    }

    private static Collection<? extends GrantedAuthority> extractPermissions(Jwt source) {
        return mapAuthoritiesCollection(
                source.getClaim(JwtPayload.PERMISSIONS.getClaimKey()), "PERM_");
    }

    private static Set<SimpleGrantedAuthority> mapRolesFromResourceClient(Jwt source, String clientId) {
        Object root = source.getClaim(JwtPayload.RESOURCE_ACCESS.getClaimKey());
        if (!(root instanceof java.util.Map<?, ?> resourceAccess)) {
            return Set.of();
        }
        Object clientNode = resourceAccess.get(clientId);
        if (!(clientNode instanceof java.util.Map<?, ?> clientMap)) {
            return Set.of();
        }
        return mapRolesCollection(clientMap.get(JwtPayload.ROLES.getClaimKey()));
    }

    private static Set<SimpleGrantedAuthority> mapRolesFromNestedClaim(Jwt source, String nestedClaimName) {
        Object realmObj = source.getClaim(nestedClaimName);
        if (!(realmObj instanceof java.util.Map<?, ?> realmMap)) {
            if (realmObj != null) {
                log.warn(
                        "JWT claim '{}' was {} (expected Map); realm roles may be missing. hasRole() can fail.",
                        nestedClaimName,
                        realmObj.getClass().getName());
            }
            return Set.of();
        }
        return mapRolesCollection(realmMap.get(JwtPayload.ROLES.getClaimKey()));
    }

    private static Set<SimpleGrantedAuthority> mapRolesCollection(Object rolesObj) {
        return mapAuthoritiesCollection(rolesObj, "ROLE_");
    }

    private static Set<SimpleGrantedAuthority> mapAuthoritiesCollection(Object valuesObject, String prefix) {
        if (!(valuesObject instanceof Collection<?> values)) {
            return Set.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(value -> value.startsWith(prefix)
                        ? new SimpleGrantedAuthority(value)
                        : new SimpleGrantedAuthority(prefix + value))
                .collect(Collectors.toSet());
    }
}
