package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMappingItemResponse {
    private Boolean isSuccess;
    private String code;
    private String message;
    private Long mappingItemId;

    public static AddMappingItemResponse success(Long mappingItemId) {
        return AddMappingItemResponse.builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("")
                .mappingItemId(mappingItemId)
                .build();
    }
}