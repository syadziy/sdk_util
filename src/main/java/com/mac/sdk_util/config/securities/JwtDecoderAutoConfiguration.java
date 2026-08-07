package com.mac.sdk_util.config.securities;

import com.mac.sdk_util.config.securities.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JwtDecoder.class)
@ConditionalOnMissingBean(JwtDecoder.class)
@ConditionalOnProperty(
        prefix = "sdk.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
@Conditional(OnJwtIssuerConfiguredCondition.class)
@AutoConfigureBefore(OAuth2ResourceServerAutoConfiguration.class)
public class JwtDecoderAutoConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(SecurityProperties properties, Environment environment) {
        String issuer = JwtIssuerResolver.resolve(properties, environment);
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalStateException(
                    "JWT issuer URI is required (sdk.security.jwt-issuer-uri or "
                            + "spring.security.oauth2.resourceserver.jwt.issuer-uri)");
        }
        return JwtDecoders.fromIssuerLocation(issuer);
    }
}
