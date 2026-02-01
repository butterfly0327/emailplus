package com.backend.backend.domain.mapping.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingValidation {
    private Long id;
    private Mapping mapping;
    private ValidationStatus status;
    private Long issueCodes;
    private String issueMessages;
    private LocalDateTime validatedAt;

    public enum ValidationStatus {
        PASSED, FAILED
    }
}
