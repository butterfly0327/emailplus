package com.backend.backend.domain.auth.service;

import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.auth.mapper.AccountMapper;
import com.backend.backend.global.config.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트
 *
 * 페르소나 기반 테스트 시나리오:
 * 1. 신규 사용자 (김민수) - 첫 로그인 시도
 * 2. 기존 사용자 (이영희) - 주기적 로그인/로그아웃, 비밀번호 변경
 * 3. 비밀번호 분실 사용자 (박철수) - 비밀번호 재설정
 * 4. 악의적 사용자 (해커) - 잘못된 자격 증명 시도
 * 5. 탈퇴 희망 사용자 (최지현) - 계정 삭제
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Claims claims;

    @InjectMocks
    private AuthService authService;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 86400000L);
    }

    // ========== 테스트 데이터 (페르소나별) ==========

    private Account createMinsuAccount() {
        return Account.builder()
                .id(1L)
                .email("minsu.kim@example.com")
                .username("김민수")
                .password("encodedPassword123!")
                .deleteCheck(false)
                .build();
    }

    private Account createYoungheeAccount() {
        return Account.builder()
                .id(2L)
                .email("younghee.lee@example.com")
                .username("이영희")
                .password("encodedPassword456!")
                .deleteCheck(false)
                .build();
    }

    private Account createChulsuAccount() {
        return Account.builder()
                .id(3L)
                .email("chulsu.park@example.com")
                .username("박철수")
                .password("encodedOldPassword!")
                .deleteCheck(false)
                .build();
    }

    private Account createJihyunAccount() {
        return Account.builder()
                .id(4L)
                .email("jihyun.choi@example.com")
                .username("최지현")
                .password("encodedPassword789!")
                .deleteCheck(false)
                .build();
    }

    // ========== 로그인 테스트 ==========

    @Nested
    @DisplayName("login 메서드는")
    class Login {

        @Nested
        @DisplayName("페르소나: 신규 사용자 김민수")
        class NewUserMinsu {

            @Test
            @DisplayName("올바른 이메일과 비밀번호로 첫 로그인에 성공한다")
            void firstLoginSuccess() {
                // given
                Account minsu = createMinsuAccount();
                LoginRequest request = new LoginRequest("minsu.kim@example.com", "rawPassword123!");

                given(accountMapper.findByEmail(request.getEmail())).willReturn(minsu);
                given(passwordEncoder.matches(request.getPassword(), minsu.getPassword())).willReturn(true);
                given(jwtTokenUtil.createToken(anyString(), anyLong())).willReturn("accessToken");
                given(jwtTokenUtil.createRefreshToken(anyString(), anyLong())).willReturn("refreshToken");
                given(redisTemplate.opsForValue()).willReturn(valueOperations);

                // when
                LoginResponse response = authService.login(request);

                // then
                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isEqualTo("accessToken");
                verify(valueOperations).set(
                        eq(REFRESH_TOKEN_PREFIX + minsu.getId()),
                        eq("refreshToken"),
                        anyLong(),
                        eq(TimeUnit.MILLISECONDS)
                );
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인 시도하면 예외가 발생한다")
            void loginWithWrongPassword() {
                // given
                Account minsu = createMinsuAccount();
                LoginRequest request = new LoginRequest("minsu.kim@example.com", "wrongPassword!");

                given(accountMapper.findByEmail(request.getEmail())).willReturn(minsu);
                given(passwordEncoder.matches(request.getPassword(), minsu.getPassword())).willReturn(false);

                // when & then
                assertThatThrownBy(() -> authService.login(request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("비밀번호가 일치하지 않습니다");
            }
        }

        @Nested
        @DisplayName("페르소나: 악의적 사용자 (해커)")
        class MaliciousUser {

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인 시도하면 예외가 발생한다")
            void loginWithNonExistentEmail() {
                // given
                LoginRequest request = new LoginRequest("hacker@evil.com", "anyPassword");
                given(accountMapper.findByEmail(request.getEmail())).willReturn(null);

                // when & then
                assertThatThrownBy(() -> authService.login(request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("존재하지 않는 이메일입니다");
            }

            @Test
            @DisplayName("SQL Injection 시도 형태의 이메일로 로그인 시도해도 정상 처리된다")
            void loginWithSqlInjectionAttempt() {
                // given
                LoginRequest request = new LoginRequest("' OR '1'='1", "anyPassword");
                given(accountMapper.findByEmail(request.getEmail())).willReturn(null);

                // when & then
                assertThatThrownBy(() -> authService.login(request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("존재하지 않는 이메일입니다");
            }
        }

        @Nested
        @DisplayName("페르소나: 기존 사용자 이영희")
        class ExistingUserYounghee {

            @Test
            @DisplayName("여러 번 로그인해도 각각 새로운 토큰이 발급된다")
            void multipleLogins() {
                // given
                Account younghee = createYoungheeAccount();
                LoginRequest request = new LoginRequest("younghee.lee@example.com", "rawPassword456!");

                given(accountMapper.findByEmail(request.getEmail())).willReturn(younghee);
                given(passwordEncoder.matches(request.getPassword(), younghee.getPassword())).willReturn(true);
                given(jwtTokenUtil.createToken(anyString(), anyLong()))
                        .willReturn("accessToken1")
                        .willReturn("accessToken2");
                given(jwtTokenUtil.createRefreshToken(anyString(), anyLong()))
                        .willReturn("refreshToken1")
                        .willReturn("refreshToken2");
                given(redisTemplate.opsForValue()).willReturn(valueOperations);

                // when
                LoginResponse response1 = authService.login(request);
                LoginResponse response2 = authService.login(request);

                // then
                assertThat(response1.getAccessToken()).isEqualTo("accessToken1");
                assertThat(response2.getAccessToken()).isEqualTo("accessToken2");
            }
        }
    }

    // ========== 토큰 갱신 테스트 ==========

    @Nested
    @DisplayName("refreshToken 메서드는")
    class RefreshToken {

        @Test
        @DisplayName("유효한 refresh token이 있으면 새 access token을 발급한다")
        void refreshTokenSuccess() {
            // given
            String accessToken = "validAccessToken";
            String userId = "1";

            given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
            given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
            given(claims.getSubject()).willReturn(userId);
            given(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + userId)).willReturn(true);
            given(jwtTokenUtil.createToken(eq(userId), anyLong())).willReturn("newAccessToken");

            // when
            TokenResponse response = authService.refreshToken(httpServletRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        }

        @Test
        @DisplayName("refresh token이 만료되었으면 예외가 발생한다")
        void refreshTokenExpired() {
            // given
            String accessToken = "validAccessToken";
            String userId = "1";

            given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
            given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
            given(claims.getSubject()).willReturn(userId);
            given(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(httpServletRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("만료되었습니다.");
        }
    }

    // ========== 로그아웃 테스트 ==========

    @Nested
    @DisplayName("logout 메서드는")
    class Logout {

        @Nested
        @DisplayName("페르소나: 기존 사용자 이영희")
        class ExistingUserYounghee {

            @Test
            @DisplayName("정상적으로 로그아웃하면 Redis에서 refresh token이 삭제된다")
            void logoutSuccess() {
                // given
                String accessToken = "validAccessToken";
                String userId = "2";

                given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
                given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
                given(claims.getSubject()).willReturn(userId);

                // when
                authService.logout(httpServletRequest);

                // then
                verify(redisTemplate).delete(REFRESH_TOKEN_PREFIX + userId);
            }
        }
    }

    // ========== 비밀번호 변경 테스트 ==========

    @Nested
    @DisplayName("changePassword 메서드는")
    class ChangePassword {

        @Nested
        @DisplayName("페르소나: 기존 사용자 이영희")
        class ExistingUserYounghee {

            @Test
            @DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 변경된다")
            void changePasswordSuccess() {
                // given
                Account younghee = createYoungheeAccount();
                String accessToken = "validAccessToken";
                PasswordChangeRequest changeRequest = new PasswordChangeRequest(
                        "currentPassword456!", "newSecurePassword789!"
                );

                given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
                given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
                given(claims.getSubject()).willReturn(String.valueOf(younghee.getId()));
                given(accountMapper.findById(younghee.getId())).willReturn(younghee);
                given(passwordEncoder.matches(changeRequest.getCurrentPassword(), younghee.getPassword()))
                        .willReturn(true);
                given(passwordEncoder.encode(changeRequest.getNewPassword())).willReturn("encodedNewPassword");

                // when
                authService.changePassword(httpServletRequest, changeRequest);

                // then
                verify(accountMapper).updatePassword(eq(younghee.getId()), eq("encodedNewPassword"));
            }

            @Test
            @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
            void changePasswordWithWrongCurrentPassword() {
                // given
                Account younghee = createYoungheeAccount();
                String accessToken = "validAccessToken";
                PasswordChangeRequest changeRequest = new PasswordChangeRequest(
                        "wrongCurrentPassword", "newPassword"
                );

                given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
                given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
                given(claims.getSubject()).willReturn(String.valueOf(younghee.getId()));
                given(accountMapper.findById(younghee.getId())).willReturn(younghee);
                given(passwordEncoder.matches(changeRequest.getCurrentPassword(), younghee.getPassword()))
                        .willReturn(false);

                // when & then
                assertThatThrownBy(() -> authService.changePassword(httpServletRequest, changeRequest))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("현재 비밀번호가 일치하지 않습니다");
            }
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 비밀번호 변경 시도시 예외가 발생한다")
        void changePasswordForNonExistentUser() {
            // given
            String accessToken = "validAccessToken";
            PasswordChangeRequest changeRequest = new PasswordChangeRequest("current", "new");

            given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
            given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
            given(claims.getSubject()).willReturn("999");
            given(accountMapper.findById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.changePassword(httpServletRequest, changeRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 사용자입니다");
        }
    }

    // ========== 비밀번호 재설정 테스트 ==========

    @Nested
    @DisplayName("resetPassword 메서드는")
    class ResetPassword {

        @Nested
        @DisplayName("페르소나: 비밀번호 분실 사용자 박철수")
        class ForgottenPasswordUserChulsu {

            @Test
            @DisplayName("존재하는 이메일로 비밀번호 재설정에 성공한다")
            void resetPasswordSuccess() {
                // given
                Account chulsu = createChulsuAccount();
                PasswordResetRequest resetRequest = new PasswordResetRequest(
                        "chulsu.park@example.com", "newPassword123!"
                );

                given(accountMapper.findByEmail(resetRequest.getEmail())).willReturn(chulsu);
                given(passwordEncoder.encode(resetRequest.getNewPassword())).willReturn("encodedNewPassword");

                // when
                authService.resetPassword(resetRequest);

                // then
                verify(accountMapper).updatePassword(eq(chulsu.getId()), eq("encodedNewPassword"));
            }

            @Test
            @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시도시 예외가 발생한다")
            void resetPasswordWithNonExistentEmail() {
                // given
                PasswordResetRequest resetRequest = new PasswordResetRequest(
                        "unknown@example.com", "newPassword"
                );

                given(accountMapper.findByEmail(resetRequest.getEmail())).willReturn(null);

                // when & then
                assertThatThrownBy(() -> authService.resetPassword(resetRequest))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("존재하지 않는 이메일입니다");
            }
        }
    }

    // ========== 계정 삭제 테스트 ==========

    @Nested
    @DisplayName("deleteAccount 메서드는")
    class DeleteAccount {

        @Nested
        @DisplayName("페르소나: 탈퇴 희망 사용자 최지현")
        class WithdrawingUserJihyun {

            @Test
            @DisplayName("정상적으로 계정을 삭제(소프트 삭제)한다")
            void deleteAccountSuccess() {
                // given
                Account jihyun = createJihyunAccount();
                String accessToken = "validAccessToken";
                String userId = String.valueOf(jihyun.getId());

                given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
                given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
                given(claims.getSubject()).willReturn(userId);
                given(accountMapper.findById(jihyun.getId())).willReturn(jihyun);

                // when
                authService.deleteAccount(httpServletRequest);

                // then
                verify(redisTemplate).delete(REFRESH_TOKEN_PREFIX + userId);
                verify(accountMapper).delete(jihyun.getId());
            }

            @Test
            @DisplayName("이미 삭제된 계정 삭제 시도시 예외가 발생한다")
            void deleteAlreadyDeletedAccount() {
                // given
                String accessToken = "validAccessToken";
                String userId = "999";

                given(jwtTokenUtil.resolveAccessToken(httpServletRequest)).willReturn(accessToken);
                given(jwtTokenUtil.getClaims(accessToken)).willReturn(claims);
                given(claims.getSubject()).willReturn(userId);
                given(accountMapper.findById(999L)).willReturn(null);

                // when & then
                assertThatThrownBy(() -> authService.deleteAccount(httpServletRequest))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("존재하지 않는 사용자입니다");
            }
        }
    }

    // ========== 엣지 케이스 테스트 ==========

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("빈 이메일로 로그인 시도하면 존재하지 않는 이메일 예외가 발생한다")
        void loginWithEmptyEmail() {
            // given
            LoginRequest request = new LoginRequest("", "password");
            given(accountMapper.findByEmail("")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 이메일입니다");
        }

        @Test
        @DisplayName("null 이메일로 로그인 시도하면 존재하지 않는 이메일 예외가 발생한다")
        void loginWithNullEmail() {
            // given
            LoginRequest request = new LoginRequest(null, "password");
            given(accountMapper.findByEmail(null)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 이메일입니다");
        }
    }
}
