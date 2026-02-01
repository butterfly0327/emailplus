package com.backend.backend.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;

    @NotBlank(message = "인증 토큰은 필수입니다")
    private String verificationToken;

}
