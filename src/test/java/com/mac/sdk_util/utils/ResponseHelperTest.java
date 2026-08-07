package com.mac.sdk_util.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ResponseHelperTest {

    @Test
    void httpCreatedWrapsBodyAndSetsLocation() {
        URI location = URI.create("/api/v1/alerts/123");

        var response = ResponseHelper.httpCreated("created-alert", location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(location, response.getHeaders().getLocation());
        assertNotNull(response.getBody());
        assertEquals("RC-201", response.getBody().getCode());
        assertEquals("created-alert", response.getBody().getData());
    }

    @Test
    void httpAcceptedWrapsBody() {
        var response = ResponseHelper.httpAccepted("accepted-alert");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RC-202", response.getBody().getCode());
        assertEquals("accepted-alert", response.getBody().getData());
    }

    @Test
    void httpConflictWrapsBody() {
        var response = ResponseHelper.httpConflict("conflicting-alert");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RC-409", response.getBody().getCode());
        assertEquals("conflicting-alert", response.getBody().getData());
    }
}
