package com.mac.sdk_util.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.mac.sdk_util.entities.dto.ResponseDTO;
import com.mac.sdk_util.entities.dto.PagingDTO;
import com.mac.sdk_util.entities.constant.Status;
import com.mac.sdk_util.entities.constant.StatusCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePagingHelper<T> {

    public static <T> ResponseEntity<ResponseDTO<T>> httpOK(T body, PagingDTO paging) {
        Status status = Status.getValueOf(StatusCode.RC_SUCCESS);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setData(body);
        result.setPaging(paging);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpNotFound() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_NOT_FOUND);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setPaging(null);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpMethodNotAllowed() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_METHOD_NOT_ALLOWED);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setPaging(null);
        return new ResponseEntity<>(result, status.getHttpCode());
    }

    public static <T> ResponseEntity<ResponseDTO<T>> httpBadRequest() {
        Status status = Status.getValueOf(StatusCode.RC_ERR_BAD_REQUEST);
        ResponseDTO<T> result = new ResponseDTO<>();
        result.setCode(status.getRespCode());
        result.setMessage(status.getMessage());
        result.setPaging(null);
        return new ResponseEntity<>(result, status.getHttpCode());
    }
}
