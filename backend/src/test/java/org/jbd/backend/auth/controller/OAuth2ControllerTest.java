package org.jbd.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.dto.OAuth2LoginRequest;
import org.jbd.backend.auth.service.OAuth2Service;
import org.jbd.backend.user.dto.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OAuth2 컨트롤러 테스트")
class OAuth2ControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private OAuth2Service oauth2Service;
    
    @Test
    @DisplayName("Google 로그인이 성공한다")
    @WithMockUser
    void authenticateWithGoogle_Success() throws Exception {
        // given
        OAuth2LoginRequest request = new OAuth2LoginRequest("valid.google.idtoken");
        
        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .email("user@gmail.com")
                .name("Test User")
                .build();
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("access.token")
                .refreshToken("refresh.token")
                .user(userDto)
                .build();
        
        given(oauth2Service.authenticateWithGoogle(any(OAuth2LoginRequest.class))).willReturn(response);
        
        // when & then
        mockMvc.perform(post("/api/auth/oauth2/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Google 로그인 성공"))
                .andExpect(jsonPath("$.data.access_token").value("access.token"))
                .andExpect(jsonPath("$.data.refresh_token").value("refresh.token"))
                .andExpect(jsonPath("$.data.user.email").value("user@gmail.com"));
    }
    
    @Test
    @DisplayName("잘못된 ID 토큰으로 Google 로그인 실패한다")
    @WithMockUser
    void authenticateWithGoogle_InvalidToken_BadRequest() throws Exception {
        // given
        OAuth2LoginRequest request = new OAuth2LoginRequest("");
        
        // when & then
        mockMvc.perform(post("/api/auth/oauth2/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Google 인증 URL 조회가 성공한다")
    void getGoogleAuthUrl_Success() throws Exception {
        // given
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test&redirect_uri=http://localhost:3000/auth/callback&scope=openid%20profile%20email&response_type=code&state=random";
        given(oauth2Service.getGoogleAuthorizationUrl(anyString(), anyString())).willReturn(authUrl);
        
        // when & then
        mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Google 인증 URL 생성 성공"))
                .andExpect(jsonPath("$.data").value(authUrl));
    }
    
    @Test
    @DisplayName("사용자 정의 리다이렉트 URI로 Google 인증 URL 조회가 성공한다")
    void getGoogleAuthUrl_CustomRedirectUri_Success() throws Exception {
        // given
        String customRedirectUri = "http://localhost:3001/oauth/callback";
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test&redirect_uri=" + customRedirectUri + "&scope=openid%20profile%20email&response_type=code&state=random";
        given(oauth2Service.getGoogleAuthorizationUrl(anyString(), anyString())).willReturn(authUrl);
        
        // when & then
        mockMvc.perform(get("/api/auth/oauth2/google/url")
                        .param("redirectUri", customRedirectUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(authUrl));
    }
}