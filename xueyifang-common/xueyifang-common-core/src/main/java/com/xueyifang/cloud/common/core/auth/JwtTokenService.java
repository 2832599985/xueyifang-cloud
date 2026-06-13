package com.xueyifang.cloud.common.core.auth;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class JwtTokenService {

    private static final String USER_ID_CLAIM = "userId";

    private static final String ROLE_CLAIM = "role";

    private static final String PUBLISH_PERMISSION_CLAIM = "publishPermission";

    private final JwtTokenProperties properties;

    private final SecretKey secretKey;

    private final Clock clock;

    public JwtTokenService(JwtTokenProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public JwtTokenService(JwtTokenProperties properties, Clock clock) {
        this.properties = validateProperties(properties);
        this.secretKey = buildSecretKey(this.properties.getSecret());
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public JwtToken createToken(Long userId, Integer role, Integer publishPermission) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        Instant issuedAt = clock.instant();
        Duration expiration = properties.getExpiration();
        Instant expiresAt = issuedAt.plus(expiration);

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(USER_ID_CLAIM, userId);

        if (properties.getIssuer() != null && !properties.getIssuer().isBlank()) {
            builder.issuer(properties.getIssuer().trim());
        }
        if (role != null) {
            builder.claim(ROLE_CLAIM, role);
        }
        if (publishPermission != null) {
            builder.claim(PUBLISH_PERMISSION_CLAIM, publishPermission);
        }

        String token = builder.signWith(secretKey, Jwts.SIG.HS256).compact();
        return new JwtToken(token, AuthConstants.TOKEN_TYPE, expiresAt, expiration.toSeconds());
    }

    public JwtToken refreshToken(String token) {
        JwtTokenClaims claims = parseToken(token);
        return createToken(claims.userId(), claims.role(), claims.publishPermission());
    }

    public JwtTokenClaims parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token无效");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();

            Long userId = resolveUserId(claims);
            if (userId == null) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token缺少用户身份");
            }

            return new JwtTokenClaims(
                    userId,
                    readIntegerClaim(claims, ROLE_CLAIM),
                    readIntegerClaim(claims, PUBLISH_PERMISSION_CLAIM),
                    toInstant(claims.getIssuedAt()),
                    toInstant(claims.getExpiration()));
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "Token已过期");
        } catch (BusinessException exception) {
            throw exception;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token无效");
        }
    }

    private static JwtTokenProperties validateProperties(JwtTokenProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("jwt token properties must not be null");
        }
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new IllegalArgumentException("jwt secret must not be blank");
        }
        if (properties.getExpiration() == null || properties.getExpiration().isZero()
                || properties.getExpiration().isNegative()) {
            throw new IllegalArgumentException("jwt expiration must be positive");
        }
        return properties;
    }

    private static SecretKey buildSecretKey(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("jwt secret must contain at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    private Long resolveUserId(Claims claims) {
        String subject = claims.getSubject();
        if (subject != null && !subject.isBlank()) {
            return Long.parseLong(subject);
        }

        return readLongClaim(claims, USER_ID_CLAIM);
    }

    private Long readLongClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer readIntegerClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
