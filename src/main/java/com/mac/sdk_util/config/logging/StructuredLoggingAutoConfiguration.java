package com.mac.sdk_util.config.logging;

import com.mac.sdk_util.config.logging.properties.StructuredLoggingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "sdk.logging.structured", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(StructuredLoggingProperties.class)
public class StructuredLoggingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "sdk.logging.structured.filter",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "mdcLoggingFilterRegistration")
    public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilterRegistration(
            StructuredLoggingProperties properties) {
        FilterRegistrationBean<MdcLoggingFilter> registration =
                new FilterRegistrationBean<>(new MdcLoggingFilter(properties));
        registration.setOrder(properties.getFilter().getOrder());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
