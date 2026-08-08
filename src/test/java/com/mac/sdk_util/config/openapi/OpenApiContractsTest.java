package com.mac.sdk_util.config.openapi;

import static org.junit.jupiter.api.Assertions.*;

import com.mac.sdk_util.config.openapi.properties.OpenApiProperties;
import com.mac.sdk_util.openapi.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.env.*;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.*;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class OpenApiContractsTest {

    @Test
    void openApiBeanAndPrefixRegistrationsUseDocumentedValues() {
        OpenApiProperties properties = new OpenApiProperties();
        properties.setTitle("Service API");
        properties.setDescription("Description");
        properties.setVersion("2.0");
        var api = new OpenApiAutoConfiguration().openApi(properties);
        assertEquals("Service API", api.getInfo().getTitle());
        assertEquals("bearer", api.getComponents().getSecuritySchemes().get("bearerAuth").getScheme());

        OpenApiUnprefixedRedirectAutoConfiguration configuration =
                new OpenApiUnprefixedRedirectAutoConfiguration();
        assertEquals("/api", OpenApiUnprefixedRedirectAutoConfiguration.normalizePathPrefix(" api/// "));
        assertEquals("/", OpenApiUnprefixedRedirectAutoConfiguration.normalizePathPrefix("/"));
        FilterRegistrationBean<?> forward = configuration.openApiPathPrefixForwardFilterRegistration("api");
        assertTrue(forward.getUrlPatterns().contains("/api/v3/api-docs"));
        FilterRegistrationBean<?> redirect =
                configuration.unprefixedSwaggerConfigRedirectFilterRegistration("/api/");
        assertEquals(Integer.MIN_VALUE + 1, redirect.getOrder());
    }

    @Test
    void forwardingAndRedirectFiltersCoverMatchingAndFallbackPaths() throws Exception {
        var forwardFilter = new OpenApiUnprefixedRedirectAutoConfiguration.OpenApiPathPrefixForwardFilter("/api");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ctx/api/v3/api-docs/swagger-config");
        request.setContextPath("/ctx");
        MockHttpServletResponse response = new MockHttpServletResponse();
        forwardFilter.doFilter(request, response, new MockFilterChain());
        assertEquals("/v3/api-docs/swagger-config", response.getForwardedUrl());

        MockHttpServletRequest yaml = new MockHttpServletRequest("GET", "/api/v3/api-docs.yaml");
        MockHttpServletResponse yamlResponse = new MockHttpServletResponse();
        forwardFilter.doFilter(yaml, yamlResponse, new MockFilterChain());
        assertEquals("/v3/api-docs.yaml", yamlResponse.getForwardedUrl());

        MockHttpServletResponse fallback = new MockHttpServletResponse();
        forwardFilter.doFilter(new MockHttpServletRequest("GET", "/other"), fallback, new MockFilterChain());
        assertNull(fallback.getForwardedUrl());
        MockHttpServletRequest invalidContext = new MockHttpServletRequest("GET", "/other");
        invalidContext.setContextPath("/ctx");
        forwardFilter.doFilter(invalidContext, new MockHttpServletResponse(), new MockFilterChain());

        var redirectFilter =
                new OpenApiUnprefixedRedirectAutoConfiguration.UnprefixedSwaggerConfigRedirectFilter(
                        "/api/v3/api-docs/swagger-config");
        MockHttpServletResponse redirect = new MockHttpServletResponse();
        MockHttpServletRequest redirectRequest = new MockHttpServletRequest("GET", "/v3/api-docs/swagger-config");
        redirectRequest.setContextPath("/ctx");
        redirectFilter.doFilter(redirectRequest, redirect, new MockFilterChain());
        assertEquals("/ctx/api/v3/api-docs/swagger-config", redirect.getRedirectedUrl());
        MockHttpServletResponse post = new MockHttpServletResponse();
        redirectFilter.doFilter(new MockHttpServletRequest("POST", "/v3/api-docs/swagger-config"),
                post, new MockFilterChain());
        assertNull(post.getRedirectedUrl());
    }

    @Test
    void environmentProcessorAndConditionRespectPrefixContextAndOverrides() {
        OpenApiEnvironmentPostProcessor processor = new OpenApiEnvironmentPostProcessor();
        StandardEnvironment prefixed = environment(Map.of("sdk.security.path-prefix", " api/// "));
        processor.postProcessEnvironment(prefixed, new SpringApplication(Object.class));
        assertEquals("/v3/api-docs", prefixed.getProperty("springdoc.api-docs.path"));
        assertEquals("/api/swagger-ui.html", prefixed.getProperty("springdoc.swagger-ui.path"));
        assertEquals("/api/v3/api-docs", prefixed.getProperty("springdoc.swagger-ui.url"));

        StandardEnvironment context = environment(Map.of(
                "server.servlet.context-path", "/ctx", "sdk.security.path-prefix", "/api"));
        processor.postProcessEnvironment(context, new SpringApplication(Object.class));
        assertEquals("/swagger-ui.html", context.getProperty("springdoc.swagger-ui.path"));
        StandardEnvironment disabled = environment(Map.of("sdk.openapi.enabled", "false"));
        processor.postProcessEnvironment(disabled, new SpringApplication(Object.class));
        assertNull(disabled.getProperty("springdoc.api-docs.path"));
        StandardEnvironment custom = environment(Map.of(
                "springdoc.api-docs.path", "/docs", "springdoc.swagger-ui.path", "/ui",
                "springdoc.swagger-ui.config-url", "/config", "springdoc.swagger-ui.url", "/url"));
        processor.postProcessEnvironment(custom, new SpringApplication(Object.class));
        assertEquals("/docs", custom.getProperty("springdoc.api-docs.path"));

        OnPathPrefixWithoutServletContextCondition condition = new OnPathPrefixWithoutServletContextCondition();
        ConditionContext conditionContext = org.mockito.Mockito.mock(ConditionContext.class);
        org.mockito.Mockito.when(conditionContext.getEnvironment()).thenReturn(new MockEnvironment()
                .withProperty("sdk.security.path-prefix", "/api"));
        assertTrue(condition.matches(conditionContext, org.mockito.Mockito.mock(AnnotatedTypeMetadata.class)));
        org.mockito.Mockito.when(conditionContext.getEnvironment()).thenReturn(new MockEnvironment()
                .withProperty("sdk.security.path-prefix", "/api")
                .withProperty("server.servlet.context-path", "/ctx"));
        assertFalse(condition.matches(conditionContext, org.mockito.Mockito.mock(AnnotatedTypeMetadata.class)));
    }

    private static StandardEnvironment environment(Map<String, Object> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        return environment;
    }
}
