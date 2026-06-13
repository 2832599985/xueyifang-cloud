package com.xueyifang.cloud.common.core.auth;

import java.time.Duration;

public class JwtTokenProperties {

    public static final String DEFAULT_ISSUER = "xueyifang";

    public static final Duration DEFAULT_EXPIRATION = Duration.ofDays(7);

    private String secret;

    private Duration expiration = DEFAULT_EXPIRATION;

    private String issuer = DEFAULT_ISSUER;

    public JwtTokenProperties() {
    }

    public JwtTokenProperties(String secret, Duration expiration, String issuer) {
        this.secret = secret;
        this.expiration = expiration;
        this.issuer = issuer;
    }

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
