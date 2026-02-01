package com.backend.backend.domain.mapping.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gesture {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String imageUrl;
}
