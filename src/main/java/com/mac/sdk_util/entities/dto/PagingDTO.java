package com.mac.sdk_util.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagingDTO {

    private int limit;
    private long offset;

    @JsonProperty("total_record")
    private long totalRecord;
}
