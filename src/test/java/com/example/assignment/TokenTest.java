package com.example.assignment;

import com.example.assignment.config.JwtUtil;
import com.example.assignment.domain.user.enums.UserRole;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class TokenTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String rawSecret = "mytestsecretmytestsecretmytestsecretmytestsecret"; // 256-bit 이상
        String base64Secret = Base64.getEncoder().encodeToString(rawSecret.getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", base64Secret);
        jwtUtil.init();
    }

    @Test
    @DisplayName("토큰 생성 및 추출 성공")
    void create_and_extract_token_success() {
        String token = jwtUtil.createToken(1L, "user1", UserRole.USER);
        String pureToken = jwtUtil.substringToken(token);

        Claims claims = jwtUtil.extractClaims(pureToken);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("user1");
        assertThat(claims.get("userRole", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 추출 실패")
    void substring_invalid_token_format() {
        assertThatThrownBy(() -> jwtUtil.substringToken("InvalidToken"))
                .isInstanceOf(JwtException.class)
                .hasMessage("잘못된 토큰 형식입니다.");
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void expired_token_should_throw_exception() {
        // 만료된 토큰 직접 생성
        String expiredToken = "Bearer " + Jwts.builder()
                .setSubject("1")
                .claim("username", "user1")
                .claim("userRole", UserRole.USER.name())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2시간 전
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 이미 만료됨
                .signWith((Key) ReflectionTestUtils.getField(jwtUtil, "key"), SignatureAlgorithm.HS256)
                .compact();

        String pureToken = jwtUtil.substringToken(expiredToken);

        assertThatThrownBy(() -> jwtUtil.extractClaims(pureToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("유효하지 않은 서명으로 인한 검증 실패")
    void invalid_signature_should_fail() {
        JwtUtil fakeUtil = new JwtUtil();
        String otherSecret = Base64.getEncoder().encodeToString("anothersecretanothersecretanothersecret".getBytes());
        ReflectionTestUtils.setField(fakeUtil, "secretKey", otherSecret);
        fakeUtil.init();

        String validToken = jwtUtil.createToken(99L, "admin", UserRole.ADMIN);
        String pureToken = jwtUtil.substringToken(validToken);

        assertThatThrownBy(() -> fakeUtil.extractClaims(pureToken))
                .isInstanceOf(JwtException.class);
    }
}