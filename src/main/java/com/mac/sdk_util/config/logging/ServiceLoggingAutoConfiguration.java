package com.mac.sdk_util.config.logging;

import com.mac.sdk_util.config.logging.properties.ServiceLoggingProperties;
import com.mac.sdk_util.config.logging.properties.StructuredLoggingProperties;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@AutoConfiguration
@ConditionalOnClass(Advice.class)
@Conditional(OnServiceLoggingPackagesCondition.class)
@EnableConfigurationProperties({ServiceLoggingProperties.class, StructuredLoggingProperties.class})
public class ServiceLoggingAutoConfiguration {

    @Bean
    public Advisor serviceLoggingAdvisor(
            ServiceLoggingProperties properties,
            StructuredLoggingProperties structuredLoggingProperties) {
        String pointcutExpression = ServiceLoggingPointcutBuilder.build(properties.resolvedPackages());

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(pointcutExpression);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(new ServiceLoggingAdvice(structuredLoggingProperties));
        return advisor;
    }
}
