package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingDetailItemResponse {
    private Long mappingId;
    private Long actionId;
    private String actionName;
    private String actionDescription;
    private String category;
    private Long gestureId;
    private String gestureName;
}
