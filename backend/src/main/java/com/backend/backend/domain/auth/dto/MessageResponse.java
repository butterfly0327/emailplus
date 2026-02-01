package com.backend.backend.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {
    private String message;
}
