package com.xueyifang.cloud.gateway.config;

import com.xueyifang.cloud.common.core.auth.JwtTokenProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "xueyifang.auth")
public class GatewayAuthProperties {

    private boolean enabled = true;

    private Jwt jwt = new Jwt();

    private List<String> publicPaths = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/register",
            "/auth/token/refresh",
            "/service/list",
            "/service/tags",
            "/review/service/**",
            "/professional/list",
            "/trade-location/list",
            "/file/view/**",
            "/sys-config/register-status",
            "/actuator/**",
            "/health",
            "/error",
            "/favicon.ico"
    ));

    private List<String> publicGetPaths = new ArrayList<>(List.of(
            "/service/*",
            "/professional/*"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public List<String> getPublicGetPaths() {
        return publicGetPaths;
    }

    public void setPublicGetPaths(List<String> publicGetPaths) {
        this.publicGetPaths = publicGetPaths;
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
