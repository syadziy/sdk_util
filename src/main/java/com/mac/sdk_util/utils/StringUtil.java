package com.mac.sdk_util.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StringUtil {

    public static final ZoneId DEFAULT_SOURCE_ZONE = ZoneId.of("UTC");
    public static final String DEFAULT_FULL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Map<String, Object> toMap(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public static String capitalizeFirstLetter(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String convertSnakeCaseToCamel(String str) {
        StringBuilder camelCaseBuilder = new StringBuilder();

        // Split the string by underscores
        String[] parts = str.split("_");

        // Append the first part as is
        camelCaseBuilder.append(parts[0]);

        // Capitalize the first letter of each subsequent part
        for (int i = 1; i < parts.length; i++) {
            camelCaseBuilder.append(capitalizeFirstLetter(parts[i]));
        }

        return camelCaseBuilder.toString();
    }

}
