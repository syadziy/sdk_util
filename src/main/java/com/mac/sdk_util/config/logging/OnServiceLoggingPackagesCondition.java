package com.mac.sdk_util.config.logging;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

class OnServiceLoggingPackagesCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!context.getEnvironment().getProperty("sdk.logging.aspect.enabled", Boolean.class, true)) {
            return ConditionOutcome.noMatch("sdk.logging.aspect.enabled=false");
        }
        String packages = context.getEnvironment().getProperty("sdk.logging.aspect.packages", "");
        if (!StringUtils.hasText(packages)) {
            return ConditionOutcome.noMatch("sdk.logging.aspect.packages is empty");
        }
        return ConditionOutcome.match();
    }
}
