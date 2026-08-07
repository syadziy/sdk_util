package com.mac.sdk_util.config.securities;

import com.mac.sdk_util.securities.JwtAuthConverter;
import com.mac.sdk_util.config.securities.properties.JwtAuthConverterProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({JwtDecoder.class, Jwt.class})
@ConditionalOnBean(JwtDecoder.class)
@ConditionalOnProperty(
        prefix = "sdk.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(JwtAuthConverterProperties.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class OAuth2JwtBeansAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtAuthConverter.class)
    public JwtAuthConverter jwtAuthConverter(JwtAuthConverterProperties properties) {
        return new JwtAuthConverter(properties);
    }

    @Bean
    @Primary
    public JwtAuthenticationConverter primaryJwtAuthenticationConverter(
            JwtAuthConverter jwtAuthConverter, JwtAuthConverterProperties properties) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (properties.getPrincipleAttribute() != null && !properties.getPrincipleAttribute().isEmpty()) {
            converter.setPrincipalClaimName(properties.getPrincipleAttribute());
        }
        converter.setJwtGrantedAuthoritiesConverter(jwtAuthConverter::extractAuthorities);
        return converter;
    }
}
