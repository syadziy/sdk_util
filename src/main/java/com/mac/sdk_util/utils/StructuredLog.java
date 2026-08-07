package com.mac.sdk_util.utils;

import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;

public final class StructuredLog {

    private StructuredLog() {}

    public static void info(Logger log, String message, Map<String, Object> fields) {
        emit(log.atInfo(), message, fields);
    }

    public static void warn(Logger log, String message, Map<String, Object> fields) {
        emit(log.atWarn(), message, fields);
    }

    public static void error(Logger log, String message, Map<String, Object> fields) {
        emit(log.atError(), message, fields);
    }

    public static void error(Logger log, String message, Map<String, Object> fields, Throwable cause) {
        LoggingEventBuilder builder = log.atError().setCause(cause);
        emit(builder, message, fields);
    }

    public static void debug(Logger log, String message, Map<String, Object> fields) {
        emit(log.atDebug(), message, fields);
    }

    public static void withMdc(Map<String, String> fields, Runnable action) {
        Map<String, String> safeFields = fields == null ? Collections.emptyMap() : fields;
        safeFields.forEach(MDC::put);
        try {
            action.run();
        } finally {
            safeFields.keySet().forEach(MDC::remove);
        }
    }

    public static void putMdc(Map<String, String> fields) {
        if (fields == null) {
            return;
        }
        fields.forEach(MDC::put);
    }

    public static void removeMdc(Iterable<String> keys) {
        if (keys == null) {
            return;
        }
        keys.forEach(MDC::remove);
    }

    private static void emit(LoggingEventBuilder builder, String message, Map<String, Object> fields) {
        LoggingEventBuilder eventBuilder = builder.setMessage(message);
        if (fields != null) {
            fields.forEach(eventBuilder::addKeyValue);
        }
        eventBuilder.log();
    }
}
