package com.mac.sdk_util.config.securities.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.auth.converter")
public class JwtAuthConverterProperties {
    private String principleAttribute = "preferred_username";
    private String resourceId = "sdk";
}
