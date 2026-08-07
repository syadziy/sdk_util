package com.mac.sdk_util.config.securities;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableMethodSecurity.class)
@ConditionalOnProperty(
        prefix = "sdk.security",
        name = "method-security-enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableMethodSecurity
public class MethodSecurityAutoConfiguration {}
