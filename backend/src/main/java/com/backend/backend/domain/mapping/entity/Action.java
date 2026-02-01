package com.backend.backend.domain.mapping.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    private Long id;
    private String category;
    private String code;
    private String name;
    private String description;
}
