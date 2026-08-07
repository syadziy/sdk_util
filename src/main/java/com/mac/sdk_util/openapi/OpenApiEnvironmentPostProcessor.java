package com.mac.sdk_util.openapi;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class OpenApiEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "openApiSpringdocDefaults";

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty("sdk.openapi.enabled", Boolean.class, Boolean.TRUE)) {
            return;
        }
        Map<String, Object> defaults = new HashMap<>();

        String apiDocsServletPath = "/v3/api-docs";
        String swaggerUiPath = "/swagger-ui.html";
        String publicOpenApiBase = apiDocsServletPath;

        String servletContext = environment.getProperty("server.servlet.context-path", "");
        if (!StringUtils.hasText(servletContext)) {
            String pathPrefix = environment.getProperty("sdk.security.path-prefix", "");
            if (StringUtils.hasText(pathPrefix)) {
                String p = normalizePathPrefix(pathPrefix);
                publicOpenApiBase = p + "/v3/api-docs";
                swaggerUiPath = p + "/swagger-ui.html";
            }
        }

        if (!environment.containsProperty("springdoc.api-docs.path")) {
            defaults.put("springdoc.api-docs.path", apiDocsServletPath);
        }
        if (!environment.containsProperty("springdoc.swagger-ui.path")) {
            defaults.put("springdoc.swagger-ui.path", swaggerUiPath);
        }
        if (!environment.containsProperty("springdoc.swagger-ui.config-url")) {
            defaults.put("springdoc.swagger-ui.config-url", publicOpenApiBase + "/swagger-config");
        }
        if (!environment.containsProperty("springdoc.swagger-ui.url")) {
            defaults.put("springdoc.swagger-ui.url", publicOpenApiBase);
        }

        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }

    private static String normalizePathPrefix(String raw) {
        String t = raw.trim();
        if (!t.startsWith("/")) {
            t = "/" + t;
        }
        while (t.length() > 1 && t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }
}
