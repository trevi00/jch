package org.jbd.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDto registrationDto;
    private AuthenticationRequest loginDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("TestPassword123!");
        registrationDto.setName("테스트 사용자");
        registrationDto.setUserType(UserType.GENERAL);

        loginDto = new AuthenticationRequest();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("TestPassword123!");
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 인증 없이 접근 가능해야 함")
    void testRegisterWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists())
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 인증 없이 접근 가능해야 함")
    void testLoginWithoutAuth() throws Exception {
        // 먼저 회원가입
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk());

        // 로그인 시도
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists());
    }

    @Test
    @DisplayName("Google OAuth URL 생성 API 테스트 - 인증 없이 접근 가능해야 함")
    void testGoogleOAuthUrlWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/google/url")
                .param("redirectUri", "http://localhost:3003/auth/callback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Google 인증 URL 생성 성공"))
                .andExpect(jsonPath("$.data").value(containsString("accounts.google.com")));
    }

    @Test
    @DisplayName("OPTIONS 요청 테스트 - CORS Preflight 요청 허용")
    void testOptionsRequest() throws Exception {
        mockMvc.perform(options("/api/auth/register")
                .header("Origin", "http://localhost:3003")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보호된 엔드포인트 테스트 - 인증 없이 접근 시 401 반환")
    void testProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("JWT 토큰으로 보호된 엔드포인트 접근 테스트")
    void testProtectedEndpointWithJWT() throws Exception {
        // 회원가입하여 토큰 획득
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(response)
                .path("data").path("access_token").asText();

        // 토큰을 사용하여 보호된 엔드포인트 접근
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator Health 엔드포인트 테스트 - 인증 없이 접근 가능")
    void testActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("잘못된 인증 토큰으로 접근 시 401 반환")
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}