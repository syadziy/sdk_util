package com.mac.sdk_util.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mac.sdk_util.entities.dto.ResponseDTO;
import com.mac.sdk_util.helper.ResponseHelper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

public final class OAuth2ServletResponses {

    private OAuth2ServletResponses() {}

    public static void writeUnauthorized(HttpServletResponse response, ObjectMapper mapper) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, ResponseHelper.unauthorizedBody(), mapper);
    }

    public static void writeForbidden(HttpServletResponse response, ObjectMapper mapper) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, ResponseHelper.forbiddenBody(), mapper);
    }

    private static void write(
            HttpServletResponse response,
            int statusCode,
            ResponseDTO<Void> body,
            ObjectMapper mapper)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(response.getOutputStream(), body);
    }
}
