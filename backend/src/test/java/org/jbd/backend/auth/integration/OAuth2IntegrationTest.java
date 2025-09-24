package org.jbd.backend.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.OAuth2LoginRequest;
import org.jbd.backend.auth.service.OAuth2Service;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.security.oauth2.client.registration.google.client-id=test-client-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
@Transactional

@DisplayName("OAuth2 통합 테스트")
class OAuth2IntegrationTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OAuth2Service oauth2Service;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("OAuth2 서비스가 정상적으로 로드된다")
    void oauth2Service_Loads_Successfully() {
        assertThat(oauth2Service).isNotNull();
    }
    
    @Test
    @DisplayName("OAuth2 로그인 엔드포인트가 접근 가능하다")
    @WithMockUser
    void oauth2LoginEndpoint_IsAccessible() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        OAuth2LoginRequest request = new OAuth2LoginRequest("test.token");
        
        // 유효하지 않은 토큰으로 인해 4xx 에러가 발생해야 함
        mockMvc.perform(post("/api/auth/oauth2/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 잘못된 토큰으로 인한 클라이언트 오류
    }
    
    @Test
    @DisplayName("OAuth2 URL 생성 엔드포인트가 접근 가능하다")
    void oauth2UrlEndpoint_IsAccessible() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    @DisplayName("OAuth2 사용자 생성 로직이 정상 동작한다")
    void createOAuth2User_WorksCorrectly() {
        // given
        String email = "oauth2user@gmail.com";
        User user = new User();
        user.setEmail(email);
        user.setName("OAuth2 User");
        user.setUserType(UserType.GENERAL);
        user.setEmailVerified(true);
        user.setOauth2Provider("google");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // when
        User savedUser = userRepository.save(user);
        
        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.isEmailVerified()).isTrue();
        assertThat(savedUser.getOauthProvider()).isEqualTo("google");
        
        Optional<User> foundUser = userRepository.findByEmailAndIsDeletedFalse(email);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }
    
    @Test
    @DisplayName("기존 사용자 OAuth2 정보 업데이트가 정상 동작한다")
    void updateExistingUserOAuth2Info_WorksCorrectly() {
        // given
        String email = "existing@gmail.com";
        User existingUser = new User(email, "password", "Old Name", UserType.GENERAL);
        User savedUser = userRepository.save(existingUser);
        
        // when
        savedUser.setName("Updated Name");
        savedUser.setOauth2Provider("google");
        savedUser.setProfileImageUrl("https://example.com/photo.jpg");
        savedUser.setEmailVerified(true);
        savedUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(savedUser);
        
        // then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getOauthProvider()).isEqualTo("google");
        assertThat(updatedUser.getProfileImageUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(updatedUser.isEmailVerified()).isTrue();
    }
    
    @Test
    @DisplayName("OAuth2 사용자는 패스워드 없이 생성될 수 있다")
    void oauth2User_CanBeCreated_WithoutPassword() {
        // given
        String email = "nopassword@gmail.com";
        User oauthUser = new User();
        oauthUser.setEmail(email);
        oauthUser.setName("OAuth User");
        oauthUser.setUserType(UserType.GENERAL);
        oauthUser.setEmailVerified(true);
        oauthUser.setOauth2Provider("google");
        oauthUser.setPassword(null); // OAuth2 사용자는 패스워드가 없을 수 있음
        
        // when
        User savedUser = userRepository.save(oauthUser);
        
        // then
        assertThat(savedUser.getPassword()).isNull();
        assertThat(savedUser.getOauthProvider()).isEqualTo("google");
        assertThat(savedUser.isEmailVerified()).isTrue();
        
        Optional<User> foundUser = userRepository.findByEmailAndIsDeletedFalse(email);
        assertThat(foundUser).isPresent();
    }
}