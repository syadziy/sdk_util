package com.mac.sdk_util.config.openapi;

import com.mac.sdk_util.openapi.OpenApiConstants;
import com.mac.sdk_util.config.openapi.properties.OpenApiProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(
        prefix = "sdk.openapi",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(OpenApiProperties.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class OpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openApi(OpenApiProperties properties) {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(properties.getTitle())
                                .description(properties.getDescription())
                                .version(properties.getVersion()))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        OpenApiConstants.BEARER_AUTH_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(OpenApiConstants.BEARER_AUTH_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }
}
