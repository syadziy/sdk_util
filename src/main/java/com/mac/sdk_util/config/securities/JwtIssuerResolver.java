package com.mac.sdk_util.config.securities;

import com.mac.sdk_util.config.securities.properties.SecurityProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

final class JwtIssuerResolver {

    private JwtIssuerResolver() {}

    static String resolve(SecurityProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getJwtIssuerUri())) {
            return properties.getJwtIssuerUri().trim();
        }
        String springIssuer = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        return StringUtils.hasText(springIssuer) ? springIssuer.trim() : null;
    }
}
