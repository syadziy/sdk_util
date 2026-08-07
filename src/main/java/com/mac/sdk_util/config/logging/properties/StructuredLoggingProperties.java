package com.mac.sdk_util.config.logging.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sdk.logging.structured")
public class StructuredLoggingProperties {

    private boolean enabled = true;

    private String format = "ecs";

    private boolean autoConfigureFormat = true;

    private String traceHeader = "X-Correlation-Id";

    private boolean traceResponseHeader = true;

    private Filter filter = new Filter();

    @Getter
    @Setter
    public static class Filter {

        private boolean enabled = true;

        private int order = -100;
    }
}
