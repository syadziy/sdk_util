package com.mac.sdk_util.config.logging;

import com.mac.sdk_util.config.logging.properties.StructuredLoggingProperties;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import java.util.LinkedHashMap;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServiceLoggingAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAdvice.class);

    private final boolean structuredLoggingEnabled;

    ServiceLoggingAdvice(StructuredLoggingProperties structuredLoggingProperties) {
        this.structuredLoggingEnabled = structuredLoggingProperties.isEnabled();
    }

    ServiceLoggingAdvice() {
        this.structuredLoggingEnabled = true;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String className = invocation.getMethod().getDeclaringClass().getSimpleName();
        String methodName = invocation.getMethod().getName();
        long started = System.currentTimeMillis();
        try {
            Object result = invocation.proceed();
            long elapsed = System.currentTimeMillis() - started;
            logSuccess(className, methodName, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - started;
            logFailure(className, methodName, elapsed, ex);
            throw ex;
        }
    }

    private void logSuccess(String className, String methodName, long elapsed) {
        if (structuredLoggingEnabled) {
            StructuredLog.info(log, "Service method completed", serviceFields(className, methodName, elapsed, true));
            return;
        }
        log.info(
                "{}:Function:{} START: {} END: {} ELAPSED: {} ms",
                className,
                methodName,
                System.currentTimeMillis() - elapsed,
                System.currentTimeMillis(),
                elapsed);
    }

    private void logFailure(String className, String methodName, long elapsed, Throwable ex) {
        if (structuredLoggingEnabled) {
            StructuredLog.error(
                    log,
                    "Service method failed",
                    serviceFields(className, methodName, elapsed, false),
                    ex);
            return;
        }
        log.error(
                "Exception during: @{} with ex: {}",
                methodName,
                ex.toString());
    }

    private static Map<String, Object> serviceFields(
            String className, String methodName, long elapsed, boolean success) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(LogFields.EVENT_DATASET, LogFields.DATASET_SERVICE);
        fields.put(LogFields.CLASS, className);
        fields.put(LogFields.METHOD, methodName);
        fields.put(LogFields.LAYER, "service");
        fields.put(LogFields.EVENT_ACTION, methodName);
        fields.put(
                LogFields.EVENT_OUTCOME,
                success ? LogFields.OUTCOME_SUCCESS : LogFields.OUTCOME_FAILURE);
        fields.put(LogFields.EVENT_DURATION, elapsed);
        return fields;
    }
}
