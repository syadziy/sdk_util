package com.mac.sdk_util.securities;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mac.sdk_util.config.securities.properties.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityContractsTest {

    @Test
    void jwtConverterCombinesUserManagementScopesRolesPermissionsAndLegacyRoles() {
        JwtAuthConverterProperties properties = new JwtAuthConverterProperties();
        properties.setPrincipleAttribute("preferred_username");
        properties.setResourceId(" api ");
        JwtAuthConverter converter = new JwtAuthConverter(properties);
        Jwt jwt = jwt(Map.of(
                "preferred_username", "ada",
                "scope", "read write",
                "roles", List.of("tenant_owner", "ROLE_operator"),
                "permissions", List.of("audit:view", "PERM_alert:create"),
                "realm_access", Map.of("roles", List.of("admin", "ROLE_user", " ", 7)),
                "resource_access", Map.of("api", Map.of("roles", Arrays.asList("client", null)))));
        Set<String> authorities = converter.extractAuthorities(jwt).stream()
                .map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
        assertTrue(authorities.containsAll(Set.of("SCOPE_read", "SCOPE_write", "ROLE_admin",
                "ROLE_user", "ROLE_7", "ROLE_client", "ROLE_tenant_owner", "ROLE_operator",
                "PERM_audit:view", "PERM_alert:create")));
        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);
        assertEquals("ada", token.getName());
        assertEquals("token", converter.getToken(jwt));

        properties.setPrincipleAttribute("");
        properties.setResourceId(" ");
        Jwt malformed = jwt(Map.of("sub", "subject", "realm_access", "wrong",
                "resource_access", "wrong"));
        assertEquals("subject", converter.convert(malformed).getName());
        assertTrue(converter.extractAuthorities(malformed).isEmpty());
        assertTrue(converter.extractAuthorities(jwt(Map.of("sub", "s", "realm_access", Map.of("roles", "bad"))))
                .isEmpty());
    }

    @Test
    void servletSecurityResponsesUseSharedJsonEnvelope() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        OAuth2ServletResponses.writeUnauthorized(unauthorized, mapper);
        assertEquals(401, unauthorized.getStatus());
        assertEquals("UTF-8", unauthorized.getCharacterEncoding());
        assertTrue(unauthorized.getContentAsString().contains("RC-401"));
        MockHttpServletResponse forbidden = new MockHttpServletResponse();
        OAuth2ServletResponses.writeForbidden(forbidden, mapper);
        assertEquals(403, forbidden.getStatus());
        assertTrue(forbidden.getContentAsString().contains("RC-403"));
    }

    private static Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2026-01-01T01:00:00Z"));
        claims.forEach(builder::claim);
        return builder.build();
    }
}
