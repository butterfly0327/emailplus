package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingItemUpdateRequest {
    private Long itemId;
    private Long mappingId;
    private Long gestureId;
    private Long actionId;
}