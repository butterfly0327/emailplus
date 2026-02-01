package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestureFixResponse {
    private Long gestureId;
    private String name;
    private String description;
    private Boolean isUsed;
    private Long actionId;
    private String actionName;
}
