package com.backend.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationScript {
    private Long id;
    private Presentation presentation;
    private String scriptText;
    private Integer version;
    private LocalDateTime createdAt;
}
