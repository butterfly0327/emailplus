package com.backend.backend.domain.auth.service;

import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.auth.mapper.AccountMapper;
import com.backend.backend.global.config.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthService 통합 테스트
 * 실제 MySQL DB와 Redis를 연동하여 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String TEST_EMAIL = "integration-test@example.com";
    private static final String TEST_PASSWORD = "testPassword123!";
    private static final String TEST_USERNAME = "IntegrationTestUser";

    private Long testAccountId;

    @BeforeEach
    void setUp() {
        // 기존 테스트 계정이 있으면 물리적 삭제
        accountMapper.hardDeleteByEmail(TEST_EMAIL);

        // 테스트용 계정 생성
        Account testAccount = Account.builder()
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .deleteCheck(false)
                .build();
        accountMapper.insert(testAccount);
        testAccountId = testAccount.getId();
        System.out.println(testAccountId);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 Redis 정리
        if (testAccountId != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + testAccountId);
        }

        // 테스트 계정 물리적 삭제
        accountMapper.hardDeleteByEmail(TEST_EMAIL);
    }

    // ========== 로그인 통합 테스트 ==========

    @Nested
    @DisplayName("로그인 통합 테스트")
    class LoginIntegrationTest {

        @Test
        @DisplayName("올바른 자격 증명으로 로그인하면 토큰이 발급되고 Redis에 refresh token이 저장된다")
        void loginSuccess_TokenIssuedAndStoredInRedis() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isNotNull();
            assertThat(response.getAccessToken()).isNotEmpty();

            // Redis에 refresh token이 저장되었는지 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            assertThat(hasKey).isTrue();

            String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);
            assertThat(storedRefreshToken).isNotNull();
            assertThat(storedRefreshToken).isNotEmpty();
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
        void loginWithWrongPassword_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongPassword");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
        void loginWithNonExistentEmail_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 이메일입니다");
        }
    }

    // ========== 토큰 갱신 통합 테스트 ==========

    @Nested
    @DisplayName("토큰 갱신 통합 테스트")
    class RefreshTokenIntegrationTest {

        @Test
        @DisplayName("유효한 refresh token이 있으면 새 access token을 발급받는다")
        void refreshToken_Success() throws InterruptedException { // [1] 예외 던지기 추가
            // given - 먼저 로그인하여 refresh token 저장
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            // [2] 여기서 1초만 멈춥니다. (JWT의 iat가 바뀌도록)
            Thread.sleep(1000);

            // access token으로 MockHttpServletRequest 생성
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            // when
            TokenResponse response = authService.refreshToken(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isNotNull();
            assertThat(response.getAccessToken()).isNotEmpty();

            // 이제 시간이 달라서 토큰 값이 달라지므로 통과합니다.
            assertThat(response.getAccessToken()).isNotEqualTo(loginResponse.getAccessToken());
        }

        @Test
        @DisplayName("refresh token이 만료(삭제)되면 예외가 발생한다")
        void refreshToken_Expired_ThrowsException() {
            // given - 먼저 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            // Redis에서 refresh token 삭제하여 만료 상황 시뮬레이션
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + testAccountId);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("만료되었습니다.");
        }
    }

    // ========== 로그아웃 통합 테스트 ==========

    @Nested
    @DisplayName("로그아웃 통합 테스트")
    class LogoutIntegrationTest {

        @Test
        @DisplayName("로그아웃하면 Redis에서 refresh token이 삭제된다")
        void logout_RefreshTokenDeletedFromRedis() {
            // given - 먼저 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            // refresh token이 Redis에 저장되어 있는지 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            // when
            authService.logout(request);

            // then - Redis에서 refresh token이 삭제되었는지 확인
            assertThat(redisTemplate.hasKey(redisKey)).isFalse();
        }
    }

    // ========== 비밀번호 변경 통합 테스트 ==========

    @Nested
    @DisplayName("비밀번호 변경 통합 테스트")
    class ChangePasswordIntegrationTest {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 비밀번호가 변경된다")
        @Transactional
        void changePassword_Success() {
            // given
            String newPassword = "newPassword456!";
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            PasswordChangeRequest changeRequest = new PasswordChangeRequest(TEST_PASSWORD, newPassword);

            // when
            authService.changePassword(request, changeRequest);

            // then - DB에서 비밀번호가 변경되었는지 확인
            Account updatedAccount = accountMapper.findById(testAccountId);
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
            assertThat(passwordEncoder.matches(TEST_PASSWORD, updatedAccount.getPassword())).isFalse();
        }

        @Test
        @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
        void changePassword_WrongCurrentPassword_ThrowsException() {
            // given
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            PasswordChangeRequest changeRequest = new PasswordChangeRequest("wrongCurrentPassword", "newPassword");

            // when & then
            assertThatThrownBy(() -> authService.changePassword(request, changeRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("현재 비밀번호가 일치하지 않습니다");
        }
    }

    // ========== 비밀번호 재설정 통합 테스트 ==========

    @Nested
    @DisplayName("비밀번호 재설정 통합 테스트")
    class ResetPasswordIntegrationTest {

        @Test
        @DisplayName("존재하는 이메일로 비밀번호 재설정에 성공한다")
        @Transactional
        void resetPassword_Success() {
            // given
            String newPassword = "resetPassword789!";
            PasswordResetRequest resetRequest = new PasswordResetRequest(TEST_EMAIL, newPassword);

            // when
            authService.resetPassword(resetRequest);

            // then - DB에서 비밀번호가 변경되었는지 확인
            Account updatedAccount = accountMapper.findById(testAccountId);
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시도하면 예외가 발생한다")
        void resetPassword_NonExistentEmail_ThrowsException() {
            // given
            PasswordResetRequest resetRequest = new PasswordResetRequest("nonexistent@example.com", "newPassword");

            // when & then
            assertThatThrownBy(() -> authService.resetPassword(resetRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 이메일입니다");
        }
    }

    // ========== 계정 삭제 통합 테스트 ==========

    @Nested
    @DisplayName("계정 삭제 통합 테스트")
    class DeleteAccountIntegrationTest {

        @Test
        @DisplayName("계정 삭제 시 soft delete가 수행되고 Redis에서 refresh token이 삭제된다")
        void deleteAccount_SoftDeleteAndRedisCleanup() {
            // given
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);

            // refresh token이 Redis에 저장되어 있는지 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());

            // when
            authService.deleteAccount(request);

            // then
            // Redis에서 refresh token 삭제 확인
            assertThat(redisTemplate.hasKey(redisKey)).isFalse();

            // DB에서 soft delete 확인 (findById는 delete_check=false인 것만 조회)
            Account deletedAccount = accountMapper.findById(testAccountId);
            assertThat(deletedAccount).isNull();
        }
    }

    // ========== 전체 플로우 통합 테스트 ==========

    @Nested
    @DisplayName("전체 인증 플로우 통합 테스트")
    class FullAuthFlowIntegrationTest {

        @Test
        @DisplayName("로그인 -> 토큰 갱신 -> 로그아웃 플로우가 정상 동작한다")
        void fullAuthFlow_LoginRefreshLogout() throws InterruptedException {
            // 1. 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);
            assertThat(loginResponse.getAccessToken()).isNotNull();

            // Redis에 refresh token 저장 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();

            Thread.sleep(1000);

            // 2. 토큰 갱신
            MockHttpServletRequest refreshRequest = new MockHttpServletRequest();
            refreshRequest.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());
            TokenResponse refreshResponse = authService.refreshToken(refreshRequest);

            assertThat(refreshResponse.getAccessToken()).isNotNull();
            assertThat(refreshResponse.getAccessToken()).isNotEqualTo(loginResponse.getAccessToken());

            // 3. 로그아웃
            MockHttpServletRequest logoutRequest = new MockHttpServletRequest();
            logoutRequest.addHeader("Authorization", "Bearer " + refreshResponse.getAccessToken());
            authService.logout(logoutRequest);

            // Redis에서 refresh token 삭제 확인
            assertThat(redisTemplate.hasKey(redisKey)).isFalse();

            // 4. 로그아웃 후 토큰 갱신 시도 -> 실패해야 함
            MockHttpServletRequest failedRefreshRequest = new MockHttpServletRequest();
            failedRefreshRequest.addHeader("Authorization", "Bearer " + refreshResponse.getAccessToken());
            assertThatThrownBy(() -> authService.refreshToken(failedRefreshRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("만료되었습니다.");
        }

        @Test
        @DisplayName("로그인 -> 비밀번호 변경 -> 새 비밀번호로 재로그인 플로우가 정상 동작한다")
        @Transactional
        void fullAuthFlow_LoginChangePasswordRelogin() {
            String newPassword = "changedPassword123!";

            // 1. 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            LoginResponse loginResponse = authService.login(loginRequest);
            System.out.println(loginResponse);


            // 2. 비밀번호 변경
            MockHttpServletRequest changeRequest = new MockHttpServletRequest();
            changeRequest.addHeader("Authorization", "Bearer " + loginResponse.getAccessToken());
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(TEST_PASSWORD, newPassword);
            authService.changePassword(changeRequest, passwordChangeRequest);

            // 3. 기존 비밀번호로 로그인 시도 -> 실패
            LoginRequest oldPasswordRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            assertThatThrownBy(() -> authService.login(oldPasswordRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다");

            // 4. 새 비밀번호로 로그인 시도 -> 성공
            LoginRequest newPasswordRequest = new LoginRequest(TEST_EMAIL, newPassword);
            LoginResponse newLoginResponse = authService.login(newPasswordRequest);
            assertThat(newLoginResponse.getAccessToken()).isNotNull();
        }
    }
}
