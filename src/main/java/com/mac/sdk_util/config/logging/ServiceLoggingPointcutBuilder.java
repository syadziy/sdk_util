package com.mac.sdk_util.config.logging;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

final class ServiceLoggingPointcutBuilder {

    private ServiceLoggingPointcutBuilder() {}

    static String build(List<String> packages) {
        if (packages == null || packages.isEmpty()) {
            return null;
        }
        String expression =
                packages.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .map(ServiceLoggingPointcutBuilder::withinExpression)
                        .collect(Collectors.joining(" || "));
        return StringUtils.hasText(expression) ? expression : null;
    }

    private static String withinExpression(String basePackage) {
        if (basePackage.endsWith("..*")) {
            return "within(" + basePackage + ")";
        }
        if (basePackage.endsWith(".*")) {
            return "within(" + basePackage + ")";
        }
        return "within(" + basePackage + "..*)";
    }
}
