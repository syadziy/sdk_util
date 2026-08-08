package com.mac.sdk_util.config.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WebExceptionHandlerAutoConfigurationTest {

    @Test
    void createsSharedHandler() {
        assertNotNull(new WebExceptionHandlerAutoConfiguration().globalExceptionHandler());
    }
}
