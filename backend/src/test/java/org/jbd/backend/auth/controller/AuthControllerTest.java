package org.jbd.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationRequest;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.service.AuthenticationService;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.common.exception.GlobalExceptionHandler;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController 테스트")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthenticationService authenticationService;
    
    @Test
    @DisplayName("회원가입 성공")
    void register_Success() throws Exception {
        // given
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test@example.com",
                "password123",
                "테스트사용자",
                UserType.GENERAL
        );
        
        UserResponseDto userResponse = UserResponseDto.from(
                new org.jbd.backend.user.domain.User(
                        registrationDto.getEmail(),
                        "encodedPassword",
                        registrationDto.getName(),
                        registrationDto.getUserType()
                )
        );
        
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(userResponse)
                .build();
        
        when(authenticationService.register(any(UserRegistrationDto.class))).thenReturn(authResponse);
        
        // when & then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").value("access-token"))
                .andExpect(jsonPath("$.data.refresh_token").value("refresh-token"));
        
        verify(authenticationService).register(any(UserRegistrationDto.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 이메일")
    void register_InvalidEmail() throws Exception {
        // given
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "invalid-email",
                "password123",
                "테스트사용자",
                UserType.GENERAL
        );
        
        // when & then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        
        verify(authenticationService, never()).register(any(UserRegistrationDto.class));
    }
    
    @Test
    @DisplayName("로그인 성공")
    void authenticate_Success() throws Exception {
        // given
        AuthenticationRequest authRequest = new AuthenticationRequest("test@example.com", "password123");
        
        UserResponseDto userResponse = UserResponseDto.from(
                new org.jbd.backend.user.domain.User(
                        authRequest.getEmail(),
                        "encodedPassword",
                        "테스트사용자",
                        UserType.GENERAL
                )
        );
        
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(userResponse)
                .build();
        
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);
        
        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").value("access-token"));
        
        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }
    
    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void authenticate_InvalidPassword() throws Exception {
        // given
        AuthenticationRequest authRequest = new AuthenticationRequest("test@example.com", "wrongpassword");
        
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PASSWORD));
        
        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_PASSWORD.getCode()));
        
        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }
    
    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() throws Exception {
        // given
        String refreshToken = "refresh-token";
        
        UserResponseDto userResponse = UserResponseDto.from(
                new org.jbd.backend.user.domain.User(
                        "test@example.com",
                        "encodedPassword",
                        "테스트사용자",
                        UserType.GENERAL
                )
        );
        
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("new-access-token")
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
        
        when(authenticationService.refreshToken(refreshToken)).thenReturn(authResponse);
        
        // when & then
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다."))
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"));
        
        verify(authenticationService).refreshToken(refreshToken);
    }
    
    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 헤더 형식")
    void refreshToken_InvalidHeader() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Invalid-Format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 형식입니다."));
        
        verify(authenticationService, never()).refreshToken(anyString());
    }
    
    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        String token = "access-token";
        
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));
        
        verify(authenticationService).logout(token);
    }
    
    @Test
    @DisplayName("로그아웃 - 헤더 없음")
    void logout_NoHeader() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));
        
        verify(authenticationService, never()).logout(anyString());
    }
}