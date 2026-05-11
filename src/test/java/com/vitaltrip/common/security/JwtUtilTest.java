package com.vitaltrip.common.security;

import com.vitaltrip.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "test-access-secret-key-must-be-at-least-32-characters!!",
                "test-refresh-secret-key-must-be-at-least-32-characters!!",
                "7d",
                "30d"
        );
        jwtUtil = new JwtUtil(props);
        jwtUtil.init();
    }

    @Test
    @DisplayName("액세스 토큰 생성·검증·userId 파싱 성공")
    void accessToken_생성_검증_userId_추출() {
        String token = jwtUtil.generateAccessToken(1L, "user@example.com");

        assertThat(jwtUtil.validateAccessToken(token)).isTrue();
        assertThat(jwtUtil.getUserIdFromAccessToken(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("액세스 토큰에 email 클레임 포함")
    void accessToken_email_클레임_포함() {
        String token = jwtUtil.generateAccessToken(42L, "test@vitaltrip.com");

        Claims claims = jwtUtil.parseAccessToken(token);
        assertThat(claims.get("email", String.class)).isEqualTo("test@vitaltrip.com");
        assertThat(claims.getSubject()).isEqualTo("42");
    }

    @Test
    @DisplayName("리프레시 토큰 생성·검증 성공")
    void refreshToken_생성_검증() {
        String token = jwtUtil.generateRefreshToken(2L, "user@example.com");

        assertThat(jwtUtil.validateRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("임시 토큰은 액세스 토큰 검증에서 거부됨")
    void tempToken_accessValidator에서_거부() {
        String tempToken = jwtUtil.generateTempToken("user@example.com", "google-id-123");

        assertThat(jwtUtil.validateAccessToken(tempToken)).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식의 액세스 토큰은 false 반환")
    void 잘못된_accessToken_false_반환() {
        assertThat(jwtUtil.validateAccessToken("invalid.token.value")).isFalse();
        assertThat(jwtUtil.validateAccessToken("")).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식의 리프레시 토큰은 false 반환")
    void 잘못된_refreshToken_false_반환() {
        assertThat(jwtUtil.validateRefreshToken("invalid.token.value")).isFalse();
    }

    @Test
    @DisplayName("액세스 토큰으로 리프레시 검증기 호출 시 false 반환 (서명 키 다름)")
    void 액세스토큰으로_리프레시검증_false() {
        String accessToken = jwtUtil.generateAccessToken(1L, "user@example.com");

        assertThat(jwtUtil.validateRefreshToken(accessToken)).isFalse();
    }

    @Test
    @DisplayName("parseDuration - 일 단위 파싱")
    void parseDuration_일_단위() {
        // 7d 만료 토큰이 정상 생성·검증되면 파싱 성공
        String token = jwtUtil.generateAccessToken(1L, "user@example.com");
        assertThat(jwtUtil.validateAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("parseDuration - 시간 단위 파싱")
    void parseDuration_시간_단위() {
        JwtProperties hProps = new JwtProperties(
                "test-access-secret-key-must-be-at-least-32-characters!!",
                "test-refresh-secret-key-must-be-at-least-32-characters!!",
                "1h",
                "720h"
        );
        JwtUtil hJwt = new JwtUtil(hProps);
        hJwt.init();

        String token = hJwt.generateAccessToken(1L, "user@example.com");
        assertThat(hJwt.validateAccessToken(token)).isTrue();
    }
}
