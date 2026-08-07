package com.mac.sdk_util.web;

import com.mac.sdk_util.entities.constant.LogFields;
import com.mac.sdk_util.entities.dto.ResponseDTO;
import com.mac.sdk_util.exception.ResourceNotFoundException;
import com.mac.sdk_util.utils.ResponseHelper;
import com.mac.sdk_util.utils.StructuredLog;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseHelper.httpNotFound(null, List.of(messageOrDefault(ex, "Resource not found")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseHelper.httpBadRequest(null, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDTO<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseHelper.httpBadRequest(
                null,
                List.of("requestBody: invalid or unreadable JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String expectedType = requiredType == null
                ? "the expected format"
                : requiredType.getSimpleName();
        return ResponseHelper.httpBadRequest(
                null,
                List.of(ex.getName() + ": must be of type " + expectedType));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDTO<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .toList();
        return ResponseHelper.httpBadRequest(null, errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseHelper.httpBadRequest(null, List.of(messageOrDefault(ex, "Invalid input")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleGenericException(Exception ex) {
        StructuredLog.error(
                LOG,
                "Unhandled HTTP request exception",
                Map.of(
                        LogFields.EVENT_ACTION, "http_request_exception",
                        LogFields.EVENT_OUTCOME, LogFields.OUTCOME_FAILURE,
                        LogFields.EVENT_DATASET, LogFields.DATASET_HTTP,
                        "error.type", ex.getClass().getName()),
                ex);
        return ResponseHelper.httpInternalServerError();
    }

    private static String messageOrDefault(Exception ex, String defaultMessage) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? defaultMessage : message;
    }
}
