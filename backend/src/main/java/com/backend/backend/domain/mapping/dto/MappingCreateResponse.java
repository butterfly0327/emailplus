package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingCreateResponse {
    private Boolean isSuccess;
    private String code;
    private String message;
    private Long mappingId;

    public static MappingCreateResponse success(Long mappingId) {
        return MappingCreateResponse.builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("")
                .mappingId(mappingId)
                .build();
    }
}