package com.mac.sdk_util.securities;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@FunctionalInterface
public interface HttpSecurityCustomizer {
    void customize(HttpSecurity http) throws Exception;
}
