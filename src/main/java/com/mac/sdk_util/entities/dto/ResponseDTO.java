package com.mac.sdk_util.entities.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDTO<T> {    
    private String code;
    private String message;
    private T data;
    private List<String> errors;

    /** Hanya diisi untuk endpoint daftar terpaginasi; dihilangkan dari JSON jika null. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PagingDTO paging;
}
