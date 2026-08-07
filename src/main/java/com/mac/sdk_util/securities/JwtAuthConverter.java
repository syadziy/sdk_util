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
        Collection<? extends GrantedAuthority> realmAndClientRoles = extractResourceRoles(jwt);
        return Stream.concat(
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                        realmAndClientRoles.stream())
                .collect(Collectors.toSet());
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        log.debug("Building JwtAuthenticationToken");

        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        if (log.isDebugEnabled()) {
            log.debug(
                    "JWT authorities: {}",
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
        }

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleClaimName(jwt));
    }

    public String getToken(Jwt jwt) {
        return jwt.getTokenValue();
    }

    private static SimpleGrantedAuthority toSpringRoleAuthority(String role) {
        if (role.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(role);
        }
        return new SimpleGrantedAuthority("ROLE_" + role);
    }

    private String getPrincipleClaimName(Jwt jwt) {
        log.debug("Resolving principal claim name");
        String claimName = JwtClaimNames.SUB;
        if (properties.getPrincipleAttribute() != null && !properties.getPrincipleAttribute().isEmpty()) {
            claimName = properties.getPrincipleAttribute();
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt source) {
        Set<SimpleGrantedAuthority> realm = mapRolesFromNestedClaim(source, JwtPayload.REALM_ACCESS.getClaimKey());
        Set<SimpleGrantedAuthority> client =
                StringUtils.hasText(properties.getResourceId())
                        ? mapRolesFromResourceClient(source, properties.getResourceId().trim())
                        : Set.of();
        return Stream.concat(realm.stream(), client.stream()).collect(Collectors.toSet());
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
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Set.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(JwtAuthConverter::toSpringRoleAuthority)
                .collect(Collectors.toSet());
    }
}
