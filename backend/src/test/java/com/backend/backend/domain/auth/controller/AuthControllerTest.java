package com.backend.backend.domain.auth.controller;

import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.service.AuthService;
import com.backend.backend.global.config.JwtTokenUtil;
import com.backend.backend.global.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트
 *
 * 페르소나 기반 테스트 시나리오:
 * 1. 신규 사용자 (김민수) - 첫 로그인 시도
 * 2. 기존 사용자 (이영희) - 로그인/로그아웃, 비밀번호 변경
 * 3. 비밀번호 분실 사용자 (박철수) - 비밀번호 재설정
 * 4. 악의적 사용자 (해커) - 잘못된 요청 시도
 * 5. 탈퇴 희망 사용자 (최지현) - 계정 삭제
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    private AuthService authService;
    
    private JwtTokenUtil jwtTokenUtil;
    
    private RedisConfig redisConfig;


    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    // ========== 로그인 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/login")
    class LoginEndpoint {

        @Nested
        @DisplayName("페르소나: 신규 사용자 김민수")
        class NewUserMinsu {

            @Test
            @DisplayName("올바른 자격 증명으로 로그인하면 200 OK와 토큰을 반환한다")
            void loginSuccess() throws Exception {
                // given
                LoginRequest request = new LoginRequest("minsu.kim@example.com", "password123!");
                LoginResponse response = LoginResponse.builder()
                        .accessToken("jwt.access.token")
                        .build();

                given(authService.login(any(LoginRequest.class))).willReturn(response);

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("jwt.access.token"));
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
            void loginWithWrongPassword() throws Exception {
                // given
                LoginRequest request = new LoginRequest("minsu.kim@example.com", "wrongPassword");
                given(authService.login(any(LoginRequest.class)))
                        .willThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다"));

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().is5xxServerError());
            }
        }

        @Nested
        @DisplayName("페르소나: 악의적 사용자 (해커)")
        class MaliciousUser {

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
            void loginWithNonExistentEmail() throws Exception {
                // given
                LoginRequest request = new LoginRequest("hacker@evil.com", "anyPassword");
                given(authService.login(any(LoginRequest.class)))
                        .willThrow(new IllegalArgumentException("존재하지 않는 이메일입니다"));

                // when & then
                mockMvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().is5xxServerError());
            }

            @Test
            @DisplayName("빈 요청 바디로 로그인하면 400 Bad Request를 반환한다")
            void loginWithEmptyBody() throws Exception {
                // when & then
                mockMvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }
    }

    // ========== 토큰 갱신 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/refresh")
    @WithMockUser
    class RefreshTokenEndpoint {

        @Test
        @DisplayName("유효한 refresh token으로 새 access token을 발급받는다")
        void refreshTokenSuccess() throws Exception {
            // given
            TokenResponse response = TokenResponse.builder()
                    .accessToken("new.access.token")
                    .build();

            given(authService.refreshToken(any(HttpServletRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/v1/auth/refresh")
                            .with(csrf())
                            .header("Authorization", "Bearer old.access.token"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new.access.token"));
        }

        @Test
        @DisplayName("만료된 refresh token으로 갱신 시도하면 예외가 발생한다")
        void refreshTokenExpired() throws Exception {
            // given
            given(authService.refreshToken(any(HttpServletRequest.class)))
                    .willThrow(new IllegalArgumentException("만료되었습니다."));

            // when & then
            mockMvc.perform(post("/v1/auth/refresh")
                            .with(csrf())
                            .header("Authorization", "Bearer expired.token"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    // ========== 로그아웃 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/logout")
    @WithMockUser
    class LogoutEndpoint {

        @Nested
        @DisplayName("페르소나: 기존 사용자 이영희")
        class ExistingUserYounghee {

            @Test
            @DisplayName("정상적으로 로그아웃하면 200 OK를 반환한다")
            void logoutSuccess() throws Exception {
                // given
                doNothing().when(authService).logout(any(HttpServletRequest.class));

                // when & then
                mockMvc.perform(post("/v1/auth/logout")
                                .with(csrf())
                                .header("Authorization", "Bearer valid.access.token"))
                        .andDo(print())
                        .andExpect(status().isOk());

                verify(authService).logout(any(HttpServletRequest.class));
            }
        }
    }

    // ========== 비밀번호 변경 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/password/change")
    @WithMockUser
    class ChangePasswordEndpoint {

        @Nested
        @DisplayName("페르소나: 기존 사용자 이영희")
        class ExistingUserYounghee {

            @Test
            @DisplayName("현재 비밀번호가 일치하면 비밀번호 변경에 성공한다")
            void changePasswordSuccess() throws Exception {
                // given
                PasswordChangeRequest request = new PasswordChangeRequest(
                        "currentPassword123!", "newPassword456!"
                );
                doNothing().when(authService).changePassword(any(HttpServletRequest.class), any(PasswordChangeRequest.class));

                // when & then
                mockMvc.perform(post("/v1/auth/password/change")
                                .with(csrf())
                                .header("Authorization", "Bearer valid.access.token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("현재 비밀번호가 틀리면 예외가 발생한다")
            void changePasswordWithWrongCurrentPassword() throws Exception {
                // given
                PasswordChangeRequest request = new PasswordChangeRequest(
                        "wrongPassword", "newPassword456!"
                );
                doThrow(new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다"))
                        .when(authService).changePassword(any(HttpServletRequest.class), any(PasswordChangeRequest.class));

                // when & then
                mockMvc.perform(post("/v1/auth/password/change")
                                .with(csrf())
                                .header("Authorization", "Bearer valid.access.token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().is5xxServerError());
            }
        }
    }

    // ========== 비밀번호 재설정 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/find/reset")
    class ResetPasswordEndpoint {

        @Nested
        @DisplayName("페르소나: 비밀번호 분실 사용자 박철수")
        class ForgottenPasswordUserChulsu {

            @Test
            @DisplayName("존재하는 이메일로 비밀번호 재설정에 성공한다")
            void resetPasswordSuccess() throws Exception {
                // given
                PasswordResetRequest request = new PasswordResetRequest(
                        "chulsu.park@example.com", "newPassword123!"
                );
                doNothing().when(authService).resetPassword(any(PasswordResetRequest.class));

                // when & then
                mockMvc.perform(post("/v1/auth/find/reset")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시도하면 예외가 발생한다")
            void resetPasswordWithNonExistentEmail() throws Exception {
                // given
                PasswordResetRequest request = new PasswordResetRequest(
                        "unknown@example.com", "newPassword123!"
                );
                doThrow(new IllegalArgumentException("존재하지 않는 이메일입니다"))
                        .when(authService).resetPassword(any(PasswordResetRequest.class));

                // when & then
                mockMvc.perform(post("/v1/auth/find/reset")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().is5xxServerError());
            }
        }
    }

    // ========== 계정 삭제 테스트 ==========

    @Nested
    @DisplayName("DELETE /v1/auth/delete")
    @WithMockUser
    class DeleteAccountEndpoint {

        @Nested
        @DisplayName("페르소나: 탈퇴 희망 사용자 최지현")
        class WithdrawingUserJihyun {

            @Test
            @DisplayName("정상적으로 계정을 삭제하면 200 OK를 반환한다")
            void deleteAccountSuccess() throws Exception {
                // given
                doNothing().when(authService).deleteAccount(any(HttpServletRequest.class));

                // when & then
                mockMvc.perform(delete("/v1/auth/delete")
                                .with(csrf())
                                .header("Authorization", "Bearer valid.access.token"))
                        .andDo(print())
                        .andExpect(status().isOk());

                verify(authService).deleteAccount(any(HttpServletRequest.class));
            }

            @Test
            @DisplayName("존재하지 않는 사용자 삭제 시도하면 예외가 발생한다")
            void deleteNonExistentAccount() throws Exception {
                // given
                doThrow(new IllegalArgumentException("존재하지 않는 사용자입니다"))
                        .when(authService).deleteAccount(any(HttpServletRequest.class));

                // when & then
                mockMvc.perform(delete("/v1/auth/delete")
                                .with(csrf())
                                .header("Authorization", "Bearer invalid.token"))
                        .andDo(print())
                        .andExpect(status().is5xxServerError());
            }
        }
    }

    // ========== 엣지 케이스 테스트 ==========

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("잘못된 JSON 형식으로 로그인 요청하면 400 Bad Request를 반환한다")
        void loginWithMalformedJson() throws Exception {
            mockMvc.perform(post("/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andDo(print())  // <--- 이 줄이 범인입니다! 삭제하세요.
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Content-Type 없이 로그인 요청하면 500 에러를 반환한다")
        void loginWithoutContentType() throws Exception {
            // when & then
            mockMvc.perform(post("/v1/auth/login")
                            .with(csrf())
                            .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }
}
