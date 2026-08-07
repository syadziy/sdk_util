package com.mac.sdk_util.config.timezone.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sdk")
public class TimezoneProperties {

    private String timezone = "";
}
