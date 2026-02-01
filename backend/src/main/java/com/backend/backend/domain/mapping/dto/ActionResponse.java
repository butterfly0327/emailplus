package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {
    private Long actionId;
    private String name;
    private String category;
    private String description;
}
