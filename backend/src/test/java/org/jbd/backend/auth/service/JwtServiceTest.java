package org.jbd.backend.auth.service;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService 테스트")
class JwtServiceTest {
    
    private JwtService jwtService;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "test-secret-key-for-jwt-should-be-at-least-256-bits-long-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400L);
        
        testUser = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }
    
    @Test
    @DisplayName("JWT 토큰 생성 성공")
    void generateToken_Success() {
        // when
        String token = jwtService.generateToken(testUser);
        
        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
    
    @Test
    @DisplayName("JWT 토큰에서 사용자명 추출 성공")
    void extractUsername_Success() {
        // given
        String token = jwtService.generateToken(testUser);
        
        // when
        String extractedUsername = jwtService.extractUsername(token);
        
        // then
        assertThat(extractedUsername).isEqualTo(testUser.getEmail());
    }
    
    @Test
    @DisplayName("JWT 토큰에서 사용자 ID 추출 성공")
    void extractUserId_Success() {
        // given
        String token = jwtService.generateToken(testUser);
        
        // when
        Long extractedUserId = jwtService.extractUserId(token);
        
        // then
        assertThat(extractedUserId).isEqualTo(testUser.getId());
    }
    
    @Test
    @DisplayName("JWT 토큰에서 사용자 타입 추출 성공")
    void extractUserType_Success() {
        // given
        String token = jwtService.generateToken(testUser);
        
        // when
        String extractedUserType = jwtService.extractUserType(token);
        
        // then
        assertThat(extractedUserType).isEqualTo(testUser.getUserType().name());
    }
    
    @Test
    @DisplayName("JWT 토큰 유효성 검사 - 유효한 토큰")
    void isTokenValid_ValidToken() {
        // given
        String token = jwtService.generateToken(testUser);
        
        // when
        boolean isValid = jwtService.isTokenValid(token, testUser);
        
        // then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("JWT 토큰 유효성 검사 - 다른 사용자")
    void isTokenValid_DifferentUser() {
        // given
        String token = jwtService.generateToken(testUser);
        User differentUser = new User("different@example.com", "password", "다른사용자", UserType.GENERAL);
        
        // when
        boolean isValid = jwtService.isTokenValid(token, differentUser);
        
        // then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void generateRefreshToken_Success() {
        // when
        String refreshToken = jwtService.generateRefreshToken(testUser);
        
        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        
        // 리프레시 토큰도 같은 사용자 정보를 포함해야 함
        String extractedUsername = jwtService.extractUsername(refreshToken);
        assertThat(extractedUsername).isEqualTo(testUser.getEmail());
    }
    
    @Test
    @DisplayName("JWT 토큰 만료 여부 확인 - 유효한 토큰")
    void isTokenExpired_ValidToken() {
        // given
        String token = jwtService.generateToken(testUser);
        
        // when
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // then
        assertThat(isExpired).isFalse();
    }
}