package com.vitaltrip.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.expires-in}")
    private String expiresIn;

    @Value("${app.jwt.refresh-expires-in}")
    private String refreshExpiresIn;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    private static final long TEMP_TOKEN_EXPIRY_MS = 10 * 60 * 1000L;

    @PostConstruct
    public void init() {
        accessKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    private long parseDurationToMs(String duration) {
        if (duration.endsWith("d")) {
            return Long.parseLong(duration.replace("d", "")) * 24 * 60 * 60 * 1000L;
        } else if (duration.endsWith("h")) {
            return Long.parseLong(duration.replace("h", "")) * 60 * 60 * 1000L;
        }
        return Long.parseLong(duration) * 1000L;
    }

    public String generateAccessToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + parseDurationToMs(expiresIn)))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + parseDurationToMs(refreshExpiresIn)))
                .signWith(refreshKey)
                .compact();
    }

    public String generateTempToken(String email, String googleId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("googleId", googleId)
                .claim("type", "TEMP")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TEMP_TOKEN_EXPIRY_MS))
                .signWith(accessKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseAccessToken(token);
            return claims.get("type") == null; // temp token 제외
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            parseRefreshToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromAccessToken(String token) {
        return Long.parseLong(parseAccessToken(token).getSubject());
    }
}
