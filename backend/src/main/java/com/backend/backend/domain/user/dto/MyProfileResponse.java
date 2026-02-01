package com.backend.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponse {
    private String email;
}
