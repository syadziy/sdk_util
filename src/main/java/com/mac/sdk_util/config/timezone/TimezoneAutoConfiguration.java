package com.mac.sdk_util.config.timezone;

import com.mac.sdk_util.config.timezone.properties.TimezoneProperties;
import com.mac.sdk_util.utils.DateUtil;
import com.mac.sdk_util.utils.StringUtil;
import java.time.ZoneId;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@AutoConfiguration
@ConditionalOnClass(DateUtil.class)
@EnableConfigurationProperties(TimezoneProperties.class)
public class TimezoneAutoConfiguration {

    @Bean
    public ZoneId applicationZone(TimezoneProperties properties, Environment environment) {
        ZoneId zone = ZoneId.of(resolveTimezoneId(properties, environment));
        DateUtil.configure(zone);
        return zone;
    }

    private static String resolveTimezoneId(TimezoneProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getTimezone())) {
            return properties.getTimezone().trim();
        }
        String jacksonZone = environment.getProperty("spring.jackson.time-zone");
        if (StringUtils.hasText(jacksonZone)) {
            return jacksonZone.trim();
        }
        return StringUtil.DEFAULT_SOURCE_ZONE.getId();
    }
}
