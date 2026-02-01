package com.backend.backend.domain.auth.controller;

import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.backend.domain.auth.dto.MessageResponse;
import com.backend.backend.domain.auth.dto.SendCodeRequest;
import com.backend.backend.domain.auth.dto.VerifyCodeRequest;
import com.backend.backend.domain.auth.dto.VerifyCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request) {
        TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody PasswordChangeRequest change) {
        authService.changePassword(request,change);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/find/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAccount(HttpServletRequest request) {
        authService.deleteAccount(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sendcode")
    public ResponseEntity<MessageResponse> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("인증번호 발송 성공")
                .build());
    }

    @PostMapping("/verifycode")
    public ResponseEntity<VerifyCodeResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String verificationToken = authService.verifyCode(request);
        return ResponseEntity.ok(VerifyCodeResponse.builder()
                .verificationToken(verificationToken)
                .build());
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        MessageResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/checkemail")
    public ResponseEntity<CheckEmailResponse> checkEmailDuplicate(@Valid @RequestBody CheckEmailRequest request) {
        CheckEmailResponse response = authService.checkEmailDuplicate(request);
        return ResponseEntity.ok(response);
    }
}
