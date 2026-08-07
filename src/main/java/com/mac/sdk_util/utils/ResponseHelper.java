package com.mac.sdk_util.utils;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.mac.sdk_util.entities.constant.Status;
import com.mac.sdk_util.entities.constant.StatusCode;
import com.mac.sdk_util.entities.dto.ResponseDTO;

public class ResponseHelper {

    public static <T> ResponseEntity<ResponseDTO<T>> httpOK() {
        Status status = Status.getValueOf(StatusCode.RC_SUCCESS);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpOK(T body) {
        Status status = Status.getValueOf(StatusCode.RC_SUCCESS);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setData(body);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpOK(T body, List<String> errors) {
        Status status = Status.getValueOf(StatusCode.RC_SUCCESS);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setData(body);
        result.setErrors(errors);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpCreated(T body, URI location) {
        ResponseDTO<T> result = fromStatus(Status.getValueOf(StatusCode.RC_CREATED));
        result.setData(body);
        return ResponseEntity.created(location).body(result);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpAccepted(T body) {
        return fromStatus(body, StatusCode.RC_ACCEPTED);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpConflict(T body) {
        return fromStatus(body, StatusCode.RC_ERR_CONFLICT);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpBadRequest() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_BAD_REQUEST);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpBadRequest(T body) {
        Status status = Status.getValueOf(StatusCode.RC_ERR_BAD_REQUEST);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setData(body);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpBadRequest(T body, List<String> errors) {
        Status status = Status.getValueOf(StatusCode.RC_ERR_BAD_REQUEST);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setData(body);
        result.setErrors(errors);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpNotFound() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_NOT_FOUND);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpNotFound(T body, List<String> errors) {
        return fromStatus(body, errors, StatusCode.RC_ERR_NOT_FOUND);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpInternalServerError() {
        return fromStatus(null, StatusCode.RC_ERR_SERVER);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpServiceUnavailable() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_SERVICE_UNAVAILABLE);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpUnauthorized() {
        ResponseDTO<T> body = unauthorizedBody();
        return new ResponseEntity<>(body, Status.getValueOf(StatusCode.RC_ERR_UNAUTHORIZED).getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpForbidden() {
        ResponseDTO<T> body = forbiddenBody();
        return new ResponseEntity<>(body, Status.getValueOf(StatusCode.RC_ERR_FORBIDDEN).getHttpCode());
    }

    public static <T> ResponseDTO<T> unauthorizedBody() {
        return fromStatus(Status.getValueOf(StatusCode.RC_ERR_UNAUTHORIZED));
    }

    public static <T> ResponseDTO<T> forbiddenBody() {
        return fromStatus(Status.getValueOf(StatusCode.RC_ERR_FORBIDDEN));
    }

    private static <T> ResponseDTO<T> fromStatus(Status status) {
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        return result;
    }

    private static <T> ResponseEntity<ResponseDTO<T>> fromStatus(T body, String statusCode) {
        Status status = Status.getValueOf(statusCode);
        ResponseDTO<T> result = fromStatus(status);
        result.setData(body);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    private static <T> ResponseEntity<ResponseDTO<T>> fromStatus(
            T body,
            List<String> errors,
            String statusCode) {
        Status status = Status.getValueOf(statusCode);
        ResponseDTO<T> result = fromStatus(status);
        result.setData(body);
        result.setErrors(errors);
        return new ResponseEntity<>(result, status.getHttpCode());
    }
}
