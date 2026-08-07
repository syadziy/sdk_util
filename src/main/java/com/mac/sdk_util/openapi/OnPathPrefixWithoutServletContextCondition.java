package com.mac.sdk_util.openapi;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

public final class OnPathPrefixWithoutServletContextCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String prefix = context.getEnvironment().getProperty("sdk.security.path-prefix", "");
        String servletContext =
                context.getEnvironment().getProperty("server.servlet.context-path", "");
        return StringUtils.hasText(prefix) && !StringUtils.hasText(servletContext);
    }
}
