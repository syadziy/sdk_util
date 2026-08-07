package com.mac.sdk_util.config.logging;

import com.mac.sdk_util.config.logging.properties.StructuredLoggingProperties;
import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.utils.StructuredLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class MdcLoggingFilter extends OncePerRequestFilter {

    private final StructuredLoggingProperties properties;

    public MdcLoggingFilter(StructuredLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        StructuredLog.putMdc(
                java.util.Map.of(
                        LogFields.TRACE_ID, traceId,
                        LogFields.EVENT_DATASET, LogFields.DATASET_HTTP));

        if (properties.isTraceResponseHeader()) {
            response.setHeader(properties.getTraceHeader(), traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            StructuredLog.removeMdc(
                    List.of(LogFields.TRACE_ID, LogFields.EVENT_DATASET));
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerValue = request.getHeader(properties.getTraceHeader());
        if (StringUtils.hasText(headerValue)) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }
}
