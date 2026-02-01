package com.backend.backend.domain.auth.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.otp")
public class OtpProperties {
    private int length = 4;
    private Duration expire = Duration.ofMinutes(3);
    private Duration verificationTokenExpire = Duration.ofMinutes(10);
}
