package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingListResponse {
    private Long presetId;
    private int orderIndex;
    private String title;
    private Boolean isRepresentative;
    private Integer gestureCount;
}
