package com.backend.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    private Long id;
    private Presentation presentation;
    private AlignmentStatus alignmentStatus;
    private BigDecimal alignmentMatchScore;
    private String alignmentErrorMessage;
    private String summary;
    private String alignmentOmission;
    private String alignmentSpeed;
    private String alignmentPronun;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public enum AlignmentStatus {
        PENDING, RUNNING, DONE, FAILED
    }
}
