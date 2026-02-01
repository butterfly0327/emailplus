package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionFixResponse {
    private Long actionId;
    private String name;
    private String category;
    private String description;
    private Boolean isUsed;
    private Long gestureId;
    private String gestureName;
}
