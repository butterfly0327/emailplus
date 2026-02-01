package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMappingResponse {
    private String code;
    private String message;
    private int status;
    private DeleteMappingData data;
    private OffsetDateTime timestamp;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteMappingData {
        private Long mappingId;
    }

    public static DeleteMappingResponse success(Long mappingId) {
        return DeleteMappingResponse.builder()
                .code("S200-004")
                .message("매핑셋이 성공적으로 삭제되었습니다.")
                .status(200)
                .data(DeleteMappingData.builder().mappingId(mappingId).build())
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
