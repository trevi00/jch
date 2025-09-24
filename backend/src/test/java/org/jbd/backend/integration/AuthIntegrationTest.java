package org.jbd.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationRequest;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 통합 테스트")
class AuthIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private UserRegistrationDto userRegistrationDto;
    private AuthenticationRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        userRegistrationDto = new UserRegistrationDto(
                "integration@test.com",
                "password123",
                "통합테스트사용자",
                UserType.GENERAL
        );
        
        loginRequest = new AuthenticationRequest(
                "integration@test.com",
                "password123"
        );
    }
    
    @Test
    @DisplayName("회원가입부터 로그인까지 전체 플로우 테스트")
    void fullAuthFlowTest() throws Exception {
        // 1. 회원가입
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists())
                .andExpect(jsonPath("$.data.user.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.user.name").value("통합테스트사용자"))
                .andExpect(jsonPath("$.data.user.userType").value("GENERAL"))
                .andReturn().getResponse().getContentAsString();
        
        // 2. 등록된 사용자로 로그인
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists())
                .andExpect(jsonPath("$.data.user.email").value("integration@test.com"))
                .andReturn().getResponse().getContentAsString();
        
        // 3. 응답에서 토큰 추출
        @SuppressWarnings("unchecked")
        ApiResponse<AuthenticationResponse> loginApiResponse = objectMapper.readValue(
                loginResponse, 
                objectMapper.getTypeFactory().constructParametricType(
                        ApiResponse.class, 
                        AuthenticationResponse.class
                )
        );
        
        String accessToken = loginApiResponse.getData().getAccessToken();
        String refreshToken = loginApiResponse.getData().getRefreshToken();
        
        // 4. 토큰 갱신 테스트
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다."))
                .andExpect(jsonPath("$.data.access_token").exists());
        
        // 5. 로그아웃 테스트
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));
    }
    
    @Test
    @DisplayName("중복 이메일 회원가입 시도")
    void duplicateEmailRegistration() throws Exception {
        // 1. 첫 번째 사용자 등록
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)))
                .andExpect(status().isOk());
        
        // 2. 동일한 이메일로 다시 등록 시도
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_002")); // 실제 코드
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도")
    void invalidPasswordLogin() throws Exception {
        // 1. 사용자 등록
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)))
                .andExpect(status().isOk());
        
        // 2. 잘못된 비밀번호로 로그인 시도
        AuthenticationRequest invalidRequest = new AuthenticationRequest(
                "integration@test.com",
                "wrongpassword"
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_004")); // 실제 코드
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시도")
    void nonExistentUserLogin() throws Exception {
        AuthenticationRequest nonExistentRequest = new AuthenticationRequest(
                "nonexistent@test.com",
                "password123"
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentRequest)))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_001")); // 실제 코드
    }
    
    @Test
    @DisplayName("유효하지 않은 이메일 형식으로 회원가입 시도")
    void invalidEmailRegistration() throws Exception {
        UserRegistrationDto invalidEmailDto = new UserRegistrationDto(
                "invalid-email-format",
                "password123",
                "테스트사용자",
                UserType.GENERAL
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    @DisplayName("잘못된 토큰으로 토큰 갱신 시도")
    void invalidRefreshToken() throws Exception {
        // 유효한 형식이지만 잘못된 서명을 가진 JWT 토큰
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature";
        
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized()) // 401 Unauthorized
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_004")); // 실제 코드
    }
    
    @Test
    @DisplayName("토큰 없이 토큰 갱신 시도")
    void refreshTokenWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", "Invalid-Format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰 형식입니다."));
    }
}