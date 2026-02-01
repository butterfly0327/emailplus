package com.backend.backend.domain.auth.controller;

import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.auth.mapper.AccountMapper;
import com.backend.backend.global.config.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String TEST_EMAIL = "controller-test@example.com";
    private static final String TEST_PASSWORD = "testPassword123!";
    private static final String TEST_USERNAME = "ControllerTestUser";

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

    // ========== 로그인 API 통합 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/login - 로그인 API")
    class LoginApiTest {

        @Test
        @DisplayName("올바른 자격 증명으로 로그인하면 200 OK와 토큰을 반환한다")
        void login_Success_ReturnsToken() throws Exception {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

            // when & then
            MvcResult result = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andReturn();

            // Redis에 refresh token이 저장되었는지 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 에러를 반환한다")
        void login_WrongPassword_ReturnsError() throws Exception {
            // given
            LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongPassword");

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인하면 에러를 반환한다")
        void login_NonExistentEmail_ReturnsError() throws Exception {
            // given
            LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("빈 요청 바디로 로그인하면 400 Bad Request를 반환한다")
        void login_EmptyBody_ReturnsBadRequest() throws Exception {
            // when & then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== 토큰 갱신 API 통합 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/refresh - 토큰 갱신 API")
    class RefreshTokenApiTest {

        @Test
        @DisplayName("유효한 refresh token이 있으면 새 access token을 발급받는다")
        void refresh_Success_ReturnsNewToken() throws Exception {
            // given - 먼저 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            // when & then
            mockMvc.perform(post("/v1/auth/refresh")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("refresh token이 만료되면 에러를 반환한다")
        void refresh_ExpiredToken_ReturnsError() throws Exception {
            // given - 먼저 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            // Redis에서 refresh token 삭제
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + testAccountId);

            // when & then
            mockMvc.perform(post("/v1/auth/refresh")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    // ========== 로그아웃 API 통합 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/logout - 로그아웃 API")
    class LogoutApiTest {

        @Test
        @DisplayName("로그아웃하면 200 OK를 반환하고 Redis에서 refresh token이 삭제된다")
        void logout_Success() throws Exception {
            // given - 먼저 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            // Redis에 refresh token이 있는지 확인
            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();

            // when
            mockMvc.perform(post("/v1/auth/logout")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                    .andDo(print())
                    .andExpect(status().isOk());

            // then - Redis에서 refresh token 삭제 확인
            assertThat(redisTemplate.hasKey(redisKey)).isFalse();
        }
    }

    // ========== 비밀번호 변경 API 통합 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/password/change - 비밀번호 변경 API")
    class ChangePasswordApiTest {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 비밀번호가 변경된다")
        @Transactional
        void changePassword_Success() throws Exception {
            // given
            String newPassword = "newPassword456!";
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            PasswordChangeRequest changeRequest = new PasswordChangeRequest(TEST_PASSWORD, newPassword);

            // when
            mockMvc.perform(post("/v1/auth/password/change")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // then - DB에서 비밀번호 변경 확인
            Account updatedAccount = accountMapper.findById(testAccountId);
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
        }

        @Test
        @DisplayName("현재 비밀번호가 틀리면 에러를 반환한다")
        void changePassword_WrongCurrentPassword_ReturnsError() throws Exception {
            // given
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            PasswordChangeRequest changeRequest = new PasswordChangeRequest("wrongPassword", "newPassword");

            // when & then
            mockMvc.perform(post("/v1/auth/password/change")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    // ========== 비밀번호 재설정 API 통합 테스트 ==========

    @Nested
    @DisplayName("POST /v1/auth/find/reset - 비밀번호 재설정 API")
    class ResetPasswordApiTest {

        @Test
        @DisplayName("존재하는 이메일로 비밀번호 재설정에 성공한다")
        @Transactional
        void resetPassword_Success() throws Exception {
            // given
            String newPassword = "resetPassword789!";
            PasswordResetRequest resetRequest = new PasswordResetRequest(TEST_EMAIL, newPassword);

            // when
            mockMvc.perform(post("/v1/auth/find/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // then - DB에서 비밀번호 변경 확인
            Account updatedAccount = accountMapper.findById(testAccountId);
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시도하면 에러를 반환한다")
        void resetPassword_NonExistentEmail_ReturnsError() throws Exception {
            // given
            PasswordResetRequest resetRequest = new PasswordResetRequest("nonexistent@example.com", "newPassword");

            // when & then
            mockMvc.perform(post("/v1/auth/find/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    // ========== 계정 삭제 API 통합 테스트 ==========

    @Nested
    @DisplayName("DELETE /v1/auth/delete - 계정 삭제 API")
    class DeleteAccountApiTest {

        @Test
        @DisplayName("계정 삭제 시 200 OK를 반환하고 soft delete가 수행된다")
        void deleteAccount_Success() throws Exception {
            // given
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            String redisKey = REFRESH_TOKEN_PREFIX + testAccountId;
            assertThat(redisTemplate.hasKey(redisKey)).isTrue();

            // when
            mockMvc.perform(delete("/v1/auth/delete")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                    .andDo(print())
                    .andExpect(status().isOk());

            // then
            // Redis에서 refresh token 삭제 확인
            assertThat(redisTemplate.hasKey(redisKey)).isFalse();

            // DB에서 soft delete 확인
            Account deletedAccount = accountMapper.findById(testAccountId);
            assertThat(deletedAccount).isNull();
        }
    }

    // ========== 전체 플로우 통합 테스트 ==========

    @Nested
    @DisplayName("전체 인증 API 플로우 통합 테스트")
    class FullAuthFlowApiTest {

        @Test
        @DisplayName("로그인 -> 토큰 갱신 -> 로그아웃 API 플로우가 정상 동작한다")
        void fullAuthFlow_Api() throws Exception {
            // 1. 로그인
            LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), LoginResponse.class);

            // 2. 토큰 갱신
            MvcResult refreshResult = mockMvc.perform(post("/v1/auth/refresh")
                            .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                    .andExpect(status().isOk())
                    .andReturn();

            TokenResponse refreshResponse = objectMapper.readValue(
                    refreshResult.getResponse().getContentAsString(), TokenResponse.class);

            // 3. 로그아웃
            mockMvc.perform(post("/v1/auth/logout")
                            .header("Authorization", "Bearer " + refreshResponse.getAccessToken()))
                    .andExpect(status().isOk());

            // 4. 로그아웃 후 토큰 갱신 시도 -> 실패해야 함
            mockMvc.perform(post("/v1/auth/refresh")
                            .header("Authorization", "Bearer " + refreshResponse.getAccessToken()))
                    .andExpect(status().is5xxServerError());
        }
    }
}
