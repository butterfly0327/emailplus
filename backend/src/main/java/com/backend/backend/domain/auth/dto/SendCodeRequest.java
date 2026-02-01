package com.backend.backend.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SendCodeRequest {
    @Email
    @NotBlank
    private String email;
}
