package com.mac.sdk_util.config.openapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sdk.openapi")
public class OpenApiProperties {

    /** When false, OpenAPI bean and springdoc path defaults from sdk-util are not applied. */
    private boolean enabled = true;

    private String title = "SDK API";

    private String description =
            "Dokumentasi REST. Untuk endpoint terproteksi JWT, gunakan Authorize dengan Bearer token.";

    private String version = "1.0.0";
}
