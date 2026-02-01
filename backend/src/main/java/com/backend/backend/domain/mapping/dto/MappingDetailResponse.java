package com.backend.backend.domain.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingDetailResponse {
    private Long presetId;
    private String title;
    private String category;
    private List<MappingDetailItemResponse> items;
}
