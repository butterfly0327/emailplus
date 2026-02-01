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
public class PresentationAudio {
    private Long id;
    private Presentation presentation;
    private String s3Key;
    private Integer durationMs;
    private Integer sampleRate;
    private LocalDateTime createdAt;
}
