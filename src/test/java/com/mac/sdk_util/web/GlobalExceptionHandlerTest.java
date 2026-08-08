package com.mac.sdk_util.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mac.sdk_util.exception.ResourceNotFoundException;
import jakarta.validation.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesKnownClientErrorsWithEnglishSafeMessages() {
        assertError(handler.handleResourceNotFound(new ResourceNotFoundException("missing")),
                HttpStatus.NOT_FOUND, "missing");
        assertError(handler.handleResourceNotFound(new ResourceNotFoundException(" ")),
                HttpStatus.NOT_FOUND, "Resource not found");

        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "email", "must be valid"));
        MethodArgumentNotValidException validation =
                new MethodArgumentNotValidException(mock(MethodParameter.class), binding);
        assertError(handler.handleValidation(validation), HttpStatus.BAD_REQUEST, "email: must be valid");
        assertError(handler.handleMessageNotReadable(new HttpMessageNotReadableException(
                        "bad", mock(org.springframework.http.HttpInputMessage.class))),
                HttpStatus.BAD_REQUEST, "requestBody: invalid or unreadable JSON");

        MethodArgumentTypeMismatchException typed = new MethodArgumentTypeMismatchException(
                "x", UUID.class, "id", mock(MethodParameter.class), null);
        assertError(handler.handleTypeMismatch(typed), HttpStatus.BAD_REQUEST, "id: must be of type UUID");
        MethodArgumentTypeMismatchException unknown = new MethodArgumentTypeMismatchException(
                "x", null, "id", mock(MethodParameter.class), null);
        assertError(handler.handleTypeMismatch(unknown), HttpStatus.BAD_REQUEST,
                "id: must be of type the expected format");

        ConstraintViolation<?> second = violation("z", "later");
        ConstraintViolation<?> first = violation("a", "first");
        var response = handler.handleConstraintViolation(new ConstraintViolationException(Set.of(second, first)));
        assertEquals(List.of("a: first", "z: later"), response.getBody().getErrors());
        assertError(handler.handleIllegalArgument(new IllegalArgumentException("invalid")),
                HttpStatus.BAD_REQUEST, "invalid");
        assertError(handler.handleIllegalArgument(new IllegalArgumentException("")),
                HttpStatus.BAD_REQUEST, "Invalid input");
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        var response = handler.handleGenericException(new IllegalStateException("secret detail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(String.valueOf(response.getBody()).contains("secret detail"));
    }

    private static ConstraintViolation<?> violation(String path, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn(path);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private static void assertError(org.springframework.http.ResponseEntity<?> response,
            HttpStatus status, String error) {
        assertEquals(status, response.getStatusCode());
        var body = (com.mac.sdk_util.entities.dto.ResponseDTO<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.getErrors().contains(error));
    }
}
