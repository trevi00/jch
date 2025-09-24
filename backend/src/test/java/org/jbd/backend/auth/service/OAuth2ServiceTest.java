package org.jbd.backend.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.dto.OAuth2LoginRequest;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2 서비스 테스트")
class OAuth2ServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private OAuth2Service oauth2Service;
    
    @Test
    @DisplayName("새로운 Google 사용자 인증이 성공한다")
    void authenticateWithGoogle_NewUser_Success() {
        // given
        String idToken = "valid.google.idtoken";
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        
        OAuth2LoginRequest request = new OAuth2LoginRequest(idToken);
        
        // Mock Google API response
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", clientId);
        tokenInfo.put("exp", String.valueOf(System.currentTimeMillis() / 1000 + 3600));
        tokenInfo.put("email", "newuser@gmail.com");
        tokenInfo.put("name", "New User");
        tokenInfo.put("picture", "https://example.com/photo.jpg");
        tokenInfo.put("email_verified", true);
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        given(userRepository.findByEmailAndIsDeletedFalse("newuser@gmail.com")).willReturn(Optional.empty());
        
        User newUser = new User("newuser@gmail.com", "password", "New User", UserType.GENERAL);
        newUser.setId(1L);
        given(userRepository.save(any(User.class))).willReturn(newUser);
        
        given(jwtService.generateToken(any(User.class))).willReturn("access.token");
        given(jwtService.generateRefreshToken(any(User.class))).willReturn("refresh.token");
        
        // when
        AuthenticationResponse response = oauth2Service.authenticateWithGoogle(request);
        
        // then
        assertThat(response.getAccessToken()).isEqualTo("access.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token");
        assertThat(response.getUser().getEmail()).isEqualTo("newuser@gmail.com");
        
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("기존 Google 사용자 인증이 성공한다")
    void authenticateWithGoogle_ExistingUser_Success() {
        // given
        String idToken = "valid.google.idtoken";
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        
        OAuth2LoginRequest request = new OAuth2LoginRequest(idToken);
        
        // Mock Google API response
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", clientId);
        tokenInfo.put("exp", String.valueOf(System.currentTimeMillis() / 1000 + 3600));
        tokenInfo.put("email", "existing@gmail.com");
        tokenInfo.put("name", "Updated User");
        tokenInfo.put("picture", "https://example.com/newphoto.jpg");
        tokenInfo.put("email_verified", true);
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        User existingUser = new User("existing@gmail.com", "password", "Old User", UserType.GENERAL);
        existingUser.setId(1L);
        given(userRepository.findByEmailAndIsDeletedFalse("existing@gmail.com")).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willReturn(existingUser);
        
        given(jwtService.generateToken(any(User.class))).willReturn("access.token");
        given(jwtService.generateRefreshToken(any(User.class))).willReturn("refresh.token");
        
        // when
        AuthenticationResponse response = oauth2Service.authenticateWithGoogle(request);
        
        // then
        assertThat(response.getAccessToken()).isEqualTo("access.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token");
        assertThat(response.getUser().getEmail()).isEqualTo("existing@gmail.com");
        
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("잘못된 Google ID 토큰으로 인증 실패한다")
    void authenticateWithGoogle_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.token";
        OAuth2LoginRequest request = new OAuth2LoginRequest(invalidToken);
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        // when & then
        assertThatThrownBy(() -> oauth2Service.authenticateWithGoogle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getMessage());
    }
    
    @Test
    @DisplayName("잘못된 audience로 토큰 검증 실패한다")
    void authenticateWithGoogle_WrongAudience_ThrowsException() {
        // given
        String idToken = "valid.google.idtoken";
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        
        OAuth2LoginRequest request = new OAuth2LoginRequest(idToken);
        
        // Mock Google API response with wrong audience
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", "wrong-client-id");
        tokenInfo.put("exp", String.valueOf(System.currentTimeMillis() / 1000 + 3600));
        tokenInfo.put("email", "user@gmail.com");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        // when & then
        assertThatThrownBy(() -> oauth2Service.authenticateWithGoogle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getMessage());
    }
    
    @Test
    @DisplayName("만료된 토큰으로 인증 실패한다")
    void authenticateWithGoogle_ExpiredToken_ThrowsException() {
        // given
        String idToken = "expired.google.idtoken";
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        
        OAuth2LoginRequest request = new OAuth2LoginRequest(idToken);
        
        // Mock Google API response with expired token
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", clientId);
        tokenInfo.put("exp", String.valueOf(System.currentTimeMillis() / 1000 - 3600)); // Expired 1 hour ago
        tokenInfo.put("email", "user@gmail.com");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        // when & then
        assertThatThrownBy(() -> oauth2Service.authenticateWithGoogle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getMessage());
    }
    
    @Test
    @DisplayName("이메일 정보가 없는 토큰으로 인증 실패한다")
    void authenticateWithGoogle_NoEmail_ThrowsException() {
        // given
        String idToken = "noemail.google.idtoken";
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        
        OAuth2LoginRequest request = new OAuth2LoginRequest(idToken);
        
        // Mock Google API response without email
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", clientId);
        tokenInfo.put("exp", String.valueOf(System.currentTimeMillis() / 1000 + 3600));
        tokenInfo.put("name", "User Name");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Map.class))).willReturn(responseEntity);
        
        // when & then
        assertThatThrownBy(() -> oauth2Service.authenticateWithGoogle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getMessage());
    }
    
    @Test
    @DisplayName("Google 인증 URL 생성이 성공한다")
    void getGoogleAuthorizationUrl_Success() {
        // given
        String clientId = "test-client-id";
        ReflectionTestUtils.setField(oauth2Service, "googleClientId", clientId);
        String redirectUri = "http://localhost:3000/auth/callback";
        String state = "random-state";
        
        // when
        String authUrl = oauth2Service.getGoogleAuthorizationUrl(redirectUri, state);
        
        // then
        assertThat(authUrl).contains("https://accounts.google.com/o/oauth2/v2/auth");
        assertThat(authUrl).contains("client_id=" + clientId);
        assertThat(authUrl).contains("redirect_uri=" + redirectUri);
        assertThat(authUrl).contains("state=" + state);
        assertThat(authUrl).contains("scope=openid%20profile%20email");
        assertThat(authUrl).contains("response_type=code");
    }
}