package com.mac.sdk_util.config.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mac.sdk_util.config.logging.properties.*;
import com.mac.sdk_util.entities.constant.LogFields;
import jakarta.servlet.FilterChain;
import java.lang.reflect.Method;
import java.util.*;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.*;
import org.springframework.mock.web.*;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.type.AnnotatedTypeMetadata;

class LoggingContractsTest {

    @AfterEach
    void clear() { MDC.clear(); }

    @Test
    void mdcFilterUsesIncomingOrGeneratedTraceAndAlwaysCleansContext() throws Exception {
        StructuredLoggingProperties properties = new StructuredLoggingProperties();
        MdcLoggingFilter filter = new MdcLoggingFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", " trace-1 ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            assertEquals("trace-1", MDC.get(LogFields.TRACE_ID));
            assertEquals(LogFields.DATASET_HTTP, MDC.get(LogFields.EVENT_DATASET));
        };
        filter.doFilter(request, response, chain);
        assertEquals("trace-1", response.getHeader("X-Correlation-Id"));
        assertNull(MDC.get(LogFields.TRACE_ID));

        properties.setTraceResponseHeader(false);
        MockHttpServletResponse noHeader = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), noHeader, (req, res) ->
                assertNotNull(UUID.fromString(MDC.get(LogFields.TRACE_ID))));
        assertNull(noHeader.getHeader("X-Correlation-Id"));
        assertThrows(IllegalStateException.class, () -> filter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> { throw new IllegalStateException("failure"); }));
        assertNull(MDC.get(LogFields.TRACE_ID));
    }

    @Test
    void pointcutPropertiesAdviceAndAutoConfigurationCoverSuccessAndFailure() throws Throwable {
        ServiceLoggingProperties properties = new ServiceLoggingProperties();
        assertTrue(properties.resolvedPackages().isEmpty());
        assertFalse(properties.hasTargetPackages());
        properties.setPackages(" com.example.service , com.example.other.* , com.example.deep..* , ");
        assertEquals(3, properties.resolvedPackages().size());
        assertTrue(properties.hasTargetPackages());
        String expression = ServiceLoggingPointcutBuilder.build(properties.resolvedPackages());
        assertTrue(expression.contains("within(com.example.service..*)"));
        assertTrue(expression.contains("within(com.example.other.*)"));
        assertTrue(expression.contains("within(com.example.deep..*)"));
        assertNull(ServiceLoggingPointcutBuilder.build(null));
        assertNull(ServiceLoggingPointcutBuilder.build(List.of(" ")));

        Method method = Demo.class.getDeclaredMethod("run");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        doReturn("result").when(invocation).proceed();
        assertEquals("result", new ServiceLoggingAdvice().invoke(invocation));
        doThrow(new IllegalStateException("boom")).when(invocation).proceed();
        assertThrows(IllegalStateException.class, () -> new ServiceLoggingAdvice().invoke(invocation));
        StructuredLoggingProperties legacyProperties = new StructuredLoggingProperties();
        legacyProperties.setEnabled(false);
        doReturn("legacy").when(invocation).proceed();
        assertEquals("legacy", new ServiceLoggingAdvice(legacyProperties).invoke(invocation));
        doThrow(new IllegalArgumentException("legacy failure")).when(invocation).proceed();
        assertThrows(IllegalArgumentException.class,
                () -> new ServiceLoggingAdvice(legacyProperties).invoke(invocation));

        StructuredLoggingProperties structured = new StructuredLoggingProperties();
        var advisor = new ServiceLoggingAutoConfiguration().serviceLoggingAdvisor(properties, structured);
        assertNotNull(((PointcutAdvisor) advisor).getPointcut());
        var registration = new StructuredLoggingAutoConfiguration().mdcLoggingFilterRegistration(structured);
        assertEquals(-100, registration.getOrder());
        assertArrayEquals(new String[] {"/*"}, registration.getUrlPatterns().toArray(String[]::new));
    }

    @Test
    void conditionAndEnvironmentDefaultsRespectConsumerConfiguration() {
        OnServiceLoggingPackagesCondition condition = new OnServiceLoggingPackagesCondition();
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        MockEnvironment environment = new MockEnvironment();
        when(context.getEnvironment()).thenReturn(environment);
        assertFalse(condition.getMatchOutcome(context, metadata).isMatch());
        environment.setProperty("sdk.logging.aspect.packages", "com.example");
        assertTrue(condition.getMatchOutcome(context, metadata).isMatch());
        environment.setProperty("sdk.logging.aspect.enabled", "false");
        assertFalse(condition.getMatchOutcome(context, metadata).isMatch());

        StructuredLoggingEnvironmentPostProcessor processor = new StructuredLoggingEnvironmentPostProcessor();
        StandardEnvironment defaults = new StandardEnvironment();
        defaults.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "sdk.logging.structured.format", " ", "spring.profiles.active", "test")));
        processor.postProcessEnvironment(defaults, mock(SpringApplication.class));
        assertEquals("ecs", defaults.getProperty("logging.structured.format.console"));
        assertEquals("ecs", defaults.getProperty("logging.structured.format.file"));
        assertEquals("test", defaults.getProperty("logging.structured.ecs.service.environment"));

        StandardEnvironment disabled = environment(Map.of("sdk.logging.structured.enabled", "false"));
        processor.postProcessEnvironment(disabled, mock(SpringApplication.class));
        assertNull(disabled.getProperty("logging.structured.format.console"));
        StandardEnvironment manual = environment(Map.of(
                "sdk.logging.structured.auto-configure-format", "false"));
        processor.postProcessEnvironment(manual, mock(SpringApplication.class));
        assertNull(manual.getProperty("logging.structured.format.console"));
        StandardEnvironment supplied = environment(Map.of(
                "logging.structured.format.console", "custom",
                "logging.structured.format.file", "custom",
                "logging.structured.ecs.service.environment", "prod"));
        processor.postProcessEnvironment(supplied, mock(SpringApplication.class));
        assertEquals("custom", supplied.getProperty("logging.structured.format.console"));
    }

    private static StandardEnvironment environment(Map<String, Object> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        return environment;
    }

    static class Demo { public String run() { return "result"; } }
}
