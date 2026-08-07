package com.mac.sdk_util.entities.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Status {

    OK                                 ( "00", "SUCCESS"),
    RC_SUCCESS                         ( HttpStatus.OK, "RC-200", "success"),
    RC_CREATED                         ( HttpStatus.CREATED, "RC-201", "created"),
    RC_ACCEPTED                        ( HttpStatus.ACCEPTED, "RC-202", "accepted"),
    RC_NO_CONTENT                      ( HttpStatus.NO_CONTENT, "RC-204", "no content"),
    RC_ERR_BAD_REQUEST                 ( HttpStatus.BAD_REQUEST, "RC-400", "bad request"),
    RC_ERR_UNAUTHORIZED                ( HttpStatus.UNAUTHORIZED, "RC-401", "unauthorization"),
    RC_ERR_FORBIDDEN                   ( HttpStatus.FORBIDDEN, "RC-403", "forbidden"),
    RC_ERR_NOT_FOUND                   ( HttpStatus.NOT_FOUND, "RC-404", "not found"),
    RC_ERR_METHOD_NOT_ALLOWED          ( HttpStatus.METHOD_NOT_ALLOWED, "RC-405", "method not allowed"),
    RC_ERR_CONFLICT                    ( HttpStatus.CONFLICT, "RC-409", "conflict"),
    RC_ERR_SERVER                      ( HttpStatus.INTERNAL_SERVER_ERROR, "RC-500", "server error"),
    RC_ERR_SERVICE_UNAVAILABLE         ( HttpStatus.SERVICE_UNAVAILABLE, "RC-503", "service unavailable"),
    UNKNOWN                            ("99", "Unknown Error");

    private HttpStatus httpCode;
    private String respCode;
    private String message;

    Status(String code, String message) {
        this.respCode = code;
        this.message = message;
    }

    Status(HttpStatus httpCode, String respCode, String message) {
        this.httpCode = httpCode;
        this.respCode = respCode;
        this.message = message;
    }

    public static Status getValueOf(String code) {
        Optional<Status> optional = Arrays.stream(Status.values())
                .filter(item -> item.getRespCode().equals(code))
                .findFirst();
        return optional.orElse(Status.UNKNOWN);
    }
}
