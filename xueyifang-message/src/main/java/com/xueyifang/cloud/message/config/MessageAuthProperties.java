package com.xueyifang.cloud.message.config;

import com.xueyifang.cloud.common.core.auth.JwtTokenProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "xueyifang.auth")
public class MessageAuthProperties {

    private Jwt jwt = new Jwt();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public JwtTokenProperties toJwtTokenProperties() {
        return new JwtTokenProperties(jwt.getSecret(), jwt.getExpiration(), jwt.getIssuer());
    }

    public static class Jwt {

        private String secret = "xueyifang-secret-key-2025-graduation-project";

        private Duration expiration = JwtTokenProperties.DEFAULT_EXPIRATION;

        private String issuer = JwtTokenProperties.DEFAULT_ISSUER;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getExpiration() {
            return expiration;
        }

        public void setExpiration(Duration expiration) {
            this.expiration = expiration;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}
