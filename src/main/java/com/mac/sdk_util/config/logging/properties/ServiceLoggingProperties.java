package com.mac.sdk_util.config.logging.properties;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "sdk.logging.aspect")
public class ServiceLoggingProperties {

    private boolean enabled = true;

    private String packages = "";

    public List<String> resolvedPackages() {
        if (!StringUtils.hasText(packages)) {
            return List.of();
        }
        return Arrays.stream(packages.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    public boolean hasTargetPackages() {
        return !resolvedPackages().isEmpty();
    }
}
