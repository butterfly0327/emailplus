package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyMappingSetResponse {
    private ApplyMappingResponse appliedPreset;
    private ApplyMappingResponse previousPreset;
    private OffsetDateTime appliedAt;
}
