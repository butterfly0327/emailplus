package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMappingItemResponse {
    private Boolean isSuccess;
    private String code;
    private String message;
    private Long deletedMappingItemId;

    public static DeleteMappingItemResponse success(Long mappingItemId) {
        return DeleteMappingItemResponse.builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("매핑 아이템 삭제 성공")
                .deletedMappingItemId(mappingItemId)
                .build();
    }
}