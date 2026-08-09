package com.mac.sdk_util.config.securities.properties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "sdk.security")
public class SecurityProperties {

    private boolean enabled = true;
    private boolean csrfDisabled = true;
    private String jwtIssuerUri;

    private String pathPrefix = "";

    private List<String> permitAllPaths =
            new ArrayList<>(
                    List.of(
                            "/health",
                            "/actuator/**",
                            "/actuator/health/**",
                            "/actuator/info/**",
                            "/actuator/metrics/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs/swagger-config",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/webjars/**",
                            "/api-docs/**",
                            "/ws/alerts",
                            "/internal/**",
                            "/error"));

    private SessionCreationPolicy sessionCreationPolicy = SessionCreationPolicy.STATELESS;

    public List<String> getPermitAllPathsResolved() {
        if (!StringUtils.hasText(pathPrefix)) {
            return new ArrayList<>(permitAllPaths);
        }
        String p = normalizePathPrefix(pathPrefix);
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (String path : permitAllPaths) {
            if (!StringUtils.hasText(path)) {
                continue;
            }
            merged.add(path);
            merged.add(p + path);
        }
        return new ArrayList<>(merged);
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
