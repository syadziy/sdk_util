package com.mac.sdk_util.config.securities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mac.sdk_util.config.securities.properties.*;
import com.mac.sdk_util.securities.JwtAuthConverter;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class SecurityConfigurationTest {

    @Test
    void securityPropertiesResolvePrefixedPathsWithoutDuplicates() {
        SecurityProperties properties = new SecurityProperties();
        assertTrue(properties.getPermitAllPathsResolved().contains("/health"));
        assertTrue(properties.getPermitAllPathsResolved().contains("/ws/alerts"));
        assertFalse(properties.getPermitAllPathsResolved().contains("/api/v1/**"));
        properties.setPathPrefix(" api/// ");
        properties.setPermitAllPaths(new ArrayList<>(List.of("/health", " ", "/health")));
        assertEquals(List.of("/health", "/api/health"), properties.getPermitAllPathsResolved());
    }

    @Test
    void issuerResolverAndConditionsSupportSdkAndSpringProperties() {
        SecurityProperties properties = new SecurityProperties();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", " https://spring ");
        assertEquals("https://spring", JwtIssuerResolver.resolve(properties, environment));
        properties.setJwtIssuerUri(" https://sdk ");
        assertEquals("https://sdk", JwtIssuerResolver.resolve(properties, environment));
        properties.setJwtIssuerUri(" ");
        assertNull(JwtIssuerResolver.resolve(properties, new MockEnvironment()));

        OnJwtIssuerConfiguredCondition condition = new OnJwtIssuerConfiguredCondition();
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(new MockEnvironment());
        assertFalse(condition.getMatchOutcome(context, mock(AnnotatedTypeMetadata.class)).isMatch());
        when(context.getEnvironment()).thenReturn(new MockEnvironment()
                .withProperty("sdk.security.jwt-issuer-uri", "https://issuer"));
        assertTrue(condition.getMatchOutcome(context, mock(AnnotatedTypeMetadata.class)).isMatch());
        when(context.getEnvironment()).thenReturn(environment);
        assertTrue(condition.getMatchOutcome(context, mock(AnnotatedTypeMetadata.class)).isMatch());
    }

    @Test
    void simpleSecurityAutoConfigurationBeansAreConstructed() {
        assertNotNull(new CorsAutoConfiguration().corsFilterRegistration().getFilter());
        JwtAuthConverterProperties converterProperties = new JwtAuthConverterProperties();
        OAuth2JwtBeansAutoConfiguration configuration = new OAuth2JwtBeansAutoConfiguration();
        JwtAuthConverter converter = configuration.jwtAuthConverter(converterProperties);
        assertNotNull(configuration.primaryJwtAuthenticationConverter(converter, converterProperties));
        converterProperties.setPrincipleAttribute("");
        assertNotNull(configuration.primaryJwtAuthenticationConverter(converter, converterProperties));
        assertNotNull(new WebSecurityPublicPathsCustomizerAutoConfiguration()
                .webSecurityPublicPathIgnoring(new SecurityProperties()));
        assertNotNull(new MethodSecurityAutoConfiguration());
        assertNotNull(new JwtDecoderAutoConfiguration());
    }
}
