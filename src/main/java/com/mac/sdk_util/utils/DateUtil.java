package com.mac.sdk_util.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TimeZone;

public final class DateUtil {

    private static volatile ZoneId applicationZone;

    private DateUtil() {}

    public static void configure(ZoneId zone) {
        ZoneId resolved = Objects.requireNonNull(zone, "zone");
        applicationZone = resolved;
        TimeZone.setDefault(TimeZone.getTimeZone(resolved));
    }

    public static ZoneId getApplicationZone() {
        ZoneId configured = applicationZone;
        return configured != null ? configured : ZoneId.systemDefault();
    }

    public static LocalDateTime getDateTimeNow() {
        return LocalDateTime.now(getApplicationZone());
    }

    public static LocalDateTime getDateTimeNow(ZoneId zone) {
        return LocalDateTime.now(Objects.requireNonNull(zone, "zone"));
    }

    public static String getDateTimeString(LocalDateTime dateTime) {
        return (dateTime != null ? dateTime : getDateTimeNow())
                .format(DateTimeFormatter.ofPattern(StringUtil.DEFAULT_FULL_DATE_TIME_FORMAT));
    }
}
