package com.xueyifang.cloud.gateway.config;

import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayAuthProperties.class)
public class GatewayAuthConfiguration {

    @Bean
    public JwtTokenService gatewayJwtTokenService(GatewayAuthProperties properties) {
        return new JwtTokenService(properties.toJwtTokenProperties());
    }
}
