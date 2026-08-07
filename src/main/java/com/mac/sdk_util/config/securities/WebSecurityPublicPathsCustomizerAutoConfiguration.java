package com.mac.sdk_util.config.securities;

import com.mac.sdk_util.config.securities.properties.SecurityProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(
        prefix = "sdk.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class WebSecurityPublicPathsCustomizerAutoConfiguration {

    @Bean
    public WebSecurityCustomizer webSecurityPublicPathIgnoring(SecurityProperties properties) {
        List<String> paths = properties.getPermitAllPathsResolved();
        String[] publicPaths = paths.toArray(String[]::new);
        return web -> web.ignoring().requestMatchers(publicPaths);
    }
}
