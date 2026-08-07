package com.mac.sdk_util.config.logging;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class StructuredLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "structuredLoggingDefaults";

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty("sdk.logging.structured.enabled", Boolean.class, Boolean.TRUE)) {
            return;
        }
        if (!environment.getProperty(
                "sdk.logging.structured.auto-configure-format", Boolean.class, Boolean.TRUE)) {
            return;
        }

        String format = environment.getProperty("sdk.logging.structured.format", "ecs");
        if (!StringUtils.hasText(format)) {
            format = "ecs";
        }

        Map<String, Object> defaults = new HashMap<>();

        if (!environment.containsProperty("logging.structured.format.console")) {
            defaults.put("logging.structured.format.console", format);
        }
        if (!environment.containsProperty("logging.structured.format.file")) {
            defaults.put("logging.structured.format.file", format);
        }
        if (!environment.containsProperty("logging.structured.ecs.service.environment")) {
            String activeProfile = environment.getProperty("spring.profiles.active", "default");
            defaults.put("logging.structured.ecs.service.environment", activeProfile);
        }

        if (!defaults.isEmpty()) {
            environment
                    .getPropertySources()
                    .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }
}
