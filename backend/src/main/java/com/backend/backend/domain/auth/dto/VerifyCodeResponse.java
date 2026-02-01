package com.backend.backend.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyCodeResponse {
    private String verificationToken;
}
