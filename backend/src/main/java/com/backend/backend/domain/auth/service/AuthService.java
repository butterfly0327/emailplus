package com.backend.backend.domain.auth.service;


import com.backend.backend.domain.auth.config.AuthMailProperties;
import com.backend.backend.domain.auth.config.OtpProperties;
import com.backend.backend.domain.auth.dto.*;
import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.auth.mapper.AccountMapper;
import com.backend.backend.global.config.JwtTokenUtil;
import com.backend.backend.global.config.RedisConfig;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountMapper accountMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisConfig redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final OtpProperties otpProperties;
    private final AuthMailProperties authMailProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.token.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.token.refresh-expiration}")
    private Long refreshExpiration;

    private static final String OTP_KEY_PREFIX = "auth:otp:";
    private static final String VERIFICATION_TOKEN_KEY_PREFIX = "auth:verification-token:";
    private static final String OTP_EMAIL_SUBJECT = "[E202] 이메일 인증번호";
    private static final String OTP_EMAIL_TEMPLATE_PATH = "templates/otp-email.html";
    private static final String LOGO_RESOURCE_PATH = "email/logo.png";
    private static final String LOGO_CONTENT_ID = "emailLogo";
    private static final int LOGO_TARGET_SIZE = 160;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private volatile byte[] cachedLogoBytes;

    public LoginResponse login(LoginRequest request) {
        Account account = accountMapper.findByEmail(request.getEmail());
        System.out.println(account);

        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다");
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenUtil.createToken(String.valueOf(account.getId()), accessExpiration);
        String refreshToken = jwtTokenUtil.createRefreshToken(account.getEmail(), refreshExpiration);

        redisTemplate.redisTemplate().opsForValue().set(
                REFRESH_TOKEN_PREFIX + account.getId(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return LoginResponse.builder()
                .message("로그인 성공")
                .accessToken(accessToken)
                .build();
    }

    public TokenResponse refreshToken(HttpServletRequest request) {
        String accessToken = jwtTokenUtil.resolveAccessToken(request);
        String userId = jwtTokenUtil.getClaims(accessToken).getSubject();
        String key = REFRESH_TOKEN_PREFIX + userId;
        if (Boolean.FALSE.equals( redisTemplate.redisTemplate().hasKey(key))) {
            throw new IllegalArgumentException("만료되었습니다.");
        }
        String token = jwtTokenUtil.createToken(userId, 1000 * 60 * 60L);
        return new TokenResponse(token);
    }

    public MessageResponse logout(HttpServletRequest request) {
        String accessToken = jwtTokenUtil.resolveAccessToken(request);
        String userId = jwtTokenUtil.getClaims(accessToken).getSubject();
        redisTemplate.redisTemplate().delete(REFRESH_TOKEN_PREFIX + userId);
        return MessageResponse.builder().message("로그아웃 성공").build();
    }

    @Transactional
    public MessageResponse changePassword(HttpServletRequest request, PasswordChangeRequest change) {
        String accessToken = jwtTokenUtil.resolveAccessToken(request);

        String userId = jwtTokenUtil.getClaims(accessToken).getSubject();
        Long id = Long.parseLong(userId);
        Account account = accountMapper.findById(id);

        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        if (!passwordEncoder.matches(change.getCurrentPassword(), account.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        validateAndConsumeVerificationToken(account.getEmail(), change.getVerificationToken());

        String encodedNewPassword = passwordEncoder.encode(change.getNewPassword());
        accountMapper.updatePassword(id, encodedNewPassword);

        return MessageResponse.builder().message("비밀번호 변경 성공").build();
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        Account account = accountMapper.findByEmail(request.getEmail());
        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다");
        }

        validateAndConsumeVerificationToken(request.getEmail(), request.getVerificationToken());

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        accountMapper.updatePassword(account.getId(), encodedNewPassword);

        return MessageResponse.builder().message("비밀번호 재설정 성공").build();
    }

    @Transactional
    public MessageResponse deleteAccount(HttpServletRequest request) {
        String accessToken = jwtTokenUtil.resolveAccessToken(request);
        String userId = jwtTokenUtil.getClaims(accessToken).getSubject();
        Long id = Long.parseLong(userId);
        Account account = accountMapper.findById(id);


        if (account == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        redisTemplate.redisTemplate().delete(REFRESH_TOKEN_PREFIX + userId);
        accountMapper.delete(id);
        return MessageResponse.builder().message("회원 탈퇴 성공").build();
    }



    public void sendCode(SendCodeRequest request) {
        String email = request.getEmail();
        String otp = generateOtp(otpProperties.getLength());
        String otpKey = OTP_KEY_PREFIX + email;

        stringRedisTemplate.delete(otpKey);
        stringRedisTemplate.opsForValue().set(otpKey, otp, otpProperties.getExpire());

        sendOtpEmail(email, otp, otpProperties.getExpire());
    }

    public String verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String otpKey = OTP_KEY_PREFIX + email;
        String storedOtp = stringRedisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new CustomException(ErrorCode.OTP_EXPIRED);
        }

        if (!storedOtp.equals(request.getOtp())) {
            throw new CustomException(ErrorCode.OTP_MISMATCH);
        }

        stringRedisTemplate.delete(otpKey);

        String verificationToken = UUID.randomUUID().toString().replace("-", "");
        String verificationKey = VERIFICATION_TOKEN_KEY_PREFIX + email;
        stringRedisTemplate.delete(verificationKey);
        stringRedisTemplate.opsForValue().set(verificationKey, verificationToken, otpProperties.getVerificationTokenExpire());

        return verificationToken;
    }

    @Transactional
    public MessageResponse signup(SignupRequest request) {
        Account existingAccount = accountMapper.findByEmail(request.getEmail());
        if (existingAccount != null) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        validateAndConsumeVerificationToken(request.getEmail(), request.getVerificationToken());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Account account = Account.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .password(encodedPassword)
                .deleteCheck(false)
                .build();
        accountMapper.insert(account);

        return MessageResponse.builder().message("회원가입 성공").build();
    }

    public CheckEmailResponse checkEmailDuplicate(CheckEmailRequest request) {
        boolean isDuplicate = accountMapper.findByEmail(request.getEmail()) != null;
        return CheckEmailResponse.builder()
                .duplicate(isDuplicate)
                .build();
    }

    private String generateOtp(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    private void sendOtpEmail(String email, String otp, Duration expire) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setFrom(authMailProperties.getFrom());
            helper.setSubject(OTP_EMAIL_SUBJECT);
            helper.setText(buildEmailBody(otp, expire), true);
            addInlineLogo(helper);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    private void validateAndConsumeVerificationToken(String email, String verificationToken) {
        String verificationKey = VERIFICATION_TOKEN_KEY_PREFIX + email;
        String storedToken = stringRedisTemplate.opsForValue().get(verificationKey);

        if (storedToken == null) {
            throw new CustomException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        if (!storedToken.equals(verificationToken)) {
            throw new CustomException(ErrorCode.VERIFICATION_TOKEN_MISMATCH);
        }

        stringRedisTemplate.delete(verificationKey);
    }

    private String buildEmailBody(String otp, Duration expire) {
        long minutes = expire.toMinutes();
        String template = loadEmailTemplate();
        return template
                .replace("{{OTP}}", otp)
                .replace("{{EXPIRE_MINUTES}}", String.valueOf(minutes))
                .replace("{{LOGO_CID}}", "cid:" + LOGO_CONTENT_ID);
    }

    private String loadEmailTemplate() {
        ClassPathResource resource = new ClassPathResource(OTP_EMAIL_TEMPLATE_PATH);
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    private void addInlineLogo(MimeMessageHelper helper) throws MessagingException {
        ClassPathResource logoResource = new ClassPathResource(LOGO_RESOURCE_PATH);
        if (logoResource.exists()) {
            try {
                helper.addInline(LOGO_CONTENT_ID, new ByteArrayResource(getOrCreateLogoBytes(logoResource)), "image/png");
            } catch (IOException e) {
                helper.addInline(LOGO_CONTENT_ID, logoResource, "image/png");
            }
        }
    }

    private byte[] getOrCreateLogoBytes(ClassPathResource logoResource) throws IOException {
        byte[] cachedBytes = cachedLogoBytes;
        if (cachedBytes != null) {
            return cachedBytes;
        }
        synchronized (this) {
            if (cachedLogoBytes == null) {
                cachedLogoBytes = resizeLogoToTarget(logoResource);
            }
            return cachedLogoBytes;
        }
    }

    private byte[] resizeLogoToTarget(ClassPathResource logoResource) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(logoResource.getInputStream());
            if (originalImage == null) {
                throw new IOException("Unable to read logo image.");
            }
            BufferedImage resizedImage = new BufferedImage(LOGO_TARGET_SIZE, LOGO_TARGET_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(originalImage, 0, 0, LOGO_TARGET_SIZE, LOGO_TARGET_SIZE, null);
            graphics.dispose();
            ImageIO.write(resizedImage, "png", outputStream);
            return outputStream.toByteArray();
        }
    }
}
