package com.xueyifang.cloud.message.config;

import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessageAuthProperties.class)
public class MessageAuthConfiguration {

    @Bean
    public JwtTokenService messageJwtTokenService(MessageAuthProperties properties) {
        return new JwtTokenService(properties.toJwtTokenProperties());
    }
}
