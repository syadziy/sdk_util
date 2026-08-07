package com.mac.sdk_util.config.securities;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

class OnJwtIssuerConfiguredCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String issuer = context.getEnvironment().getProperty("sdk.security.jwt-issuer-uri");
        String springIssuer =
                context.getEnvironment().getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        if (StringUtils.hasText(issuer) || StringUtils.hasText(springIssuer)) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch(
                "Neither sdk.security.jwt-issuer-uri nor spring.security.oauth2.resourceserver.jwt.issuer-uri is set");
    }
}
