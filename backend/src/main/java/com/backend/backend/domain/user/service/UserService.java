package com.backend.backend.domain.user.service;

import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.auth.mapper.AccountMapper;
import com.backend.backend.domain.user.dto.MyProfileResponse;
import com.backend.backend.global.config.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountMapper accountMapper;
    private final JwtTokenUtil jwtTokenUtil;

    public MyProfileResponse getMyProfile(HttpServletRequest request) {
        String accessToken = jwtTokenUtil.resolveAccessToken(request);
        String userId = jwtTokenUtil.getClaims(accessToken).getSubject();
        Long id = Long.parseLong(userId);
        Account account = accountMapper.findById(id);
        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        return MyProfileResponse.builder()
                .email(account.getEmail())
                .build();
    }
}
