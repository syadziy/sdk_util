package com.mac.sdk_util.config.timezone;

import static org.junit.jupiter.api.Assertions.*;

import com.mac.sdk_util.config.timezone.properties.TimezoneProperties;
import com.mac.sdk_util.utils.DateUtil;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class TimezoneAutoConfigurationTest {

    @Test
    void resolvesExplicitJacksonAndDefaultTimezone() {
        TimezoneAutoConfiguration configuration = new TimezoneAutoConfiguration();
        TimezoneProperties properties = new TimezoneProperties();
        properties.setTimezone(" UTC ");
        assertEquals(ZoneId.of("UTC"), configuration.applicationZone(properties, new MockEnvironment()));
        properties.setTimezone(" ");
        assertEquals(ZoneId.of("Europe/Paris"), configuration.applicationZone(properties,
                new MockEnvironment().withProperty("spring.jackson.time-zone", " Europe/Paris ")));
        assertEquals(ZoneId.of("UTC"), configuration.applicationZone(properties, new MockEnvironment()));
        assertEquals(ZoneId.of("UTC"), DateUtil.getApplicationZone());
    }
}
