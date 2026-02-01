package com.backend.backend.domain.user.controller;

import com.backend.backend.domain.user.dto.MyProfileResponse;
import com.backend.backend.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<MyProfileResponse> getMyProfile(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getMyProfile(request));
    }
}
