package com.xueyifang.cloud.auth.config;

import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthTokenConfiguration {

    @Bean
    public JwtTokenService authJwtTokenService(AuthProperties properties) {
        return new JwtTokenService(properties.toJwtTokenProperties());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
