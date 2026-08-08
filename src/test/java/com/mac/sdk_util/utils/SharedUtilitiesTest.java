package com.mac.sdk_util.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mac.sdk_util.entities.dto.PagingDTO;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.*;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpStatus;

class SharedUtilitiesTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void responseHelpersCoverEveryPublicMapping() {
        assertEquals(HttpStatus.OK, ResponseHelper.httpOK().getStatusCode());
        assertEquals("data", ResponseHelper.httpOK("data").getBody().getData());
        assertEquals(List.of("warning"), ResponseHelper.httpOK("data", List.of("warning")).getBody().getErrors());
        assertEquals(HttpStatus.BAD_REQUEST, ResponseHelper.httpBadRequest().getStatusCode());
        assertEquals("bad", ResponseHelper.httpBadRequest("bad").getBody().getData());
        assertEquals(List.of("error"), ResponseHelper.httpBadRequest("bad", List.of("error")).getBody().getErrors());
        assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.httpNotFound().getStatusCode());
        assertEquals(List.of("missing"), ResponseHelper.httpNotFound(null, List.of("missing")).getBody().getErrors());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ResponseHelper.httpInternalServerError().getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ResponseHelper.httpServiceUnavailable().getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ResponseHelper.httpUnauthorized().getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, ResponseHelper.httpForbidden().getStatusCode());
        assertEquals("RC-401", ResponseHelper.unauthorizedBody().getCode());
        assertEquals("RC-403", ResponseHelper.forbiddenBody().getCode());

        PagingDTO paging = new PagingDTO(10, 0, 20);
        assertSame(paging, ResponsePagingHelper.httpOK(List.of("x"), paging).getBody().getPaging());
        assertEquals(HttpStatus.NOT_FOUND, ResponsePagingHelper.httpNotFound().getStatusCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, ResponsePagingHelper.httpMethodNotAllowed().getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, ResponsePagingHelper.httpBadRequest().getStatusCode());
        ResponsePagingHelper<String> helper = new ResponsePagingHelper<>();
        assertNotNull(helper);
    }

    @Test
    void stringDateAndQueryUtilitiesPreserveTheirContracts() {
        record Example(String firstName, int value) {}
        assertEquals(Map.of("first_name", "Ada", "value", 2), StringUtil.toMap(new Example("Ada", 2)));
        assertEquals("Hello", StringUtil.capitalizeFirstLetter("hello"));
        assertEquals("helloWorldAgain", StringUtil.convertSnakeCaseToCamel("hello_world_again"));
        assertEquals("hello", StringUtil.convertSnakeCaseToCamel("hello"));
        DateUtil.configure(ZoneId.of("UTC"));
        assertEquals(ZoneId.of("UTC"), DateUtil.getApplicationZone());
        assertNotNull(DateUtil.getDateTimeNow());
        assertNotNull(DateUtil.getDateTimeNow(ZoneId.of("Asia/Jakarta")));
        assertEquals("2026-01-02T03:04:05.006", DateUtil.getDateTimeString(
                LocalDateTime.of(2026, 1, 2, 3, 4, 5, 6_000_000)));
        assertNotNull(DateUtil.getDateTimeString(null));
        assertThrows(NullPointerException.class, () -> DateUtil.configure(null));
        assertEquals("=", QueryUtil.FieldOperator.EQUAL);
        assertEquals("AND", QueryUtil.GroupOperator.AND);
        assertEquals("TEXT", QueryUtil.DataType.TEXT);
        assertEquals("COUNT", QueryUtil.ModifierOperator.COUNT);
        assertNotNull(new QueryUtil());
    }

    @Test
    void structuredLoggingEmitsFieldsAndRestoresMdcOnSuccessAndFailure() {
        Logger logger = mock(Logger.class);
        LoggingEventBuilder builder = mock(LoggingEventBuilder.class, RETURNS_SELF);
        when(logger.atInfo()).thenReturn(builder);
        when(logger.atWarn()).thenReturn(builder);
        when(logger.atError()).thenReturn(builder);
        when(logger.atDebug()).thenReturn(builder);
        StructuredLog.info(logger, "info", Map.of("id", 1));
        StructuredLog.warn(logger, "warn", null);
        StructuredLog.error(logger, "error", Map.of());
        RuntimeException cause = new RuntimeException("boom");
        StructuredLog.error(logger, "error", Map.of("id", 2), cause);
        StructuredLog.debug(logger, "debug", Map.of("id", 3));
        verify(builder, atLeastOnce()).log();
        verify(builder).setCause(cause);

        MDC.put("old", "value");
        StructuredLog.withMdc(Map.of("new", "value"), () -> assertEquals("value", MDC.get("new")));
        assertEquals("value", MDC.get("old"));
        assertNull(MDC.get("new"));
        assertThrows(IllegalStateException.class,
                () -> StructuredLog.withMdc(null, () -> { throw new IllegalStateException("failure"); }));
        assertEquals("value", MDC.get("old"));
        assertEquals("value", StructuredLog.copyMdc().get("old"));
        StructuredLog.putMdc(Map.of("extra", "x"));
        StructuredLog.removeMdc(List.of("extra"));
        StructuredLog.putMdc(null);
        StructuredLog.removeMdc(null);
        MDC.clear();
        assertTrue(StructuredLog.copyMdc().isEmpty());
    }
}
