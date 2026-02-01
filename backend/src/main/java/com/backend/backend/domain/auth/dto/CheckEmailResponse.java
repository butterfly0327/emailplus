package com.backend.backend.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckEmailResponse {
    @JsonProperty("isDuplicate")
    private boolean duplicate;
}
