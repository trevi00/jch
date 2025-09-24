package org.jbd.backend.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.dto.OAuth2LoginRequest;
import org.jbd.backend.auth.dto.OAuth2UserInfo;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.domain.enums.OAuthProvider;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
public class OAuth2Service {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    
    private static final String GOOGLE_TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    public OAuth2Service(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            JwtService jwtService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    public AuthenticationResponse authenticateWithGoogle(OAuth2LoginRequest request) {
        try {
            // Google ID 토큰 검증
            OAuth2UserInfo userInfo = verifyGoogleIdToken(request.getIdToken());
            
            // 사용자 조회 또는 생성
            User user = findOrCreateUser(userInfo, UserType.GENERAL);
            
            // JWT 토큰 생성
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserResponseDto.from(user))
                    .build();
                    
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
    
    private OAuth2UserInfo verifyGoogleIdToken(String idToken) {
        try {
            // Google의 tokeninfo 엔드포인트를 사용하여 토큰 검증
            String url = GOOGLE_TOKEN_INFO_URL + idToken;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.INVALID_OAUTH2_TOKEN);
            }
            
            Map<String, Object> tokenInfo = response.getBody();
            
            // 토큰의 audience(aud) 확인
            String audience = (String) tokenInfo.get("aud");
            if (!googleClientId.equals(audience)) {
                throw new BusinessException(ErrorCode.INVALID_OAUTH2_TOKEN);
            }
            
            // 토큰 만료 확인
            String exp = (String) tokenInfo.get("exp");
            if (exp != null) {
                long expTime = Long.parseLong(exp);
                if (System.currentTimeMillis() / 1000 > expTime) {
                    throw new BusinessException(ErrorCode.EXPIRED_OAUTH2_TOKEN);
                }
            }
            
            return new OAuth2UserInfo(tokenInfo);
            
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
    
    private UserType parseUserTypeFromState(String state) {
        if (state != null && state.contains("|")) {
            String[] parts = state.split("\\|");
            if (parts.length >= 2) {
                try {
                    return UserType.valueOf(parts[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid user type, default to GENERAL
                }
            }
        }
        return UserType.GENERAL;
    }
    
    private String parseActionFromState(String state) {
        if (state != null && state.contains("|")) {
            String[] parts = state.split("\\|");
            if (parts.length >= 3) {
                return parts[2].toUpperCase();
            }
        }
        return "SIGNUP"; // default action
    }
    
    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        return findOrCreateUser(userInfo, UserType.GENERAL);
    }
    
    private User findOrCreateUser(OAuth2UserInfo userInfo, UserType userType) {
        String email = userInfo.getEmail();
        
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH2_USER_INFO);
        }
        
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .map(user -> updateExistingUser(user, userInfo))
                .orElseGet(() -> createNewUser(userInfo, userType));
    }
    
    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        boolean updated = false;
        
        // UserProfile 조회 및 업데이트
        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        boolean profileUpdated = false;

        // 이름 업데이트 (프로필에서)
        if (userInfo.getName() != null) {
            if (userProfile == null) {
                userProfile = new UserProfile(user, userInfo.getName(), "");
                profileUpdated = true;
            } else if (!userInfo.getName().equals(userProfile.getFullName())) {
                userProfile.updateName(userInfo.getName(), "");
                profileUpdated = true;
            }
        }

        // 프로필 이미지 업데이트
        if (userInfo.getPicture() != null) {
            if (userProfile == null) {
                userProfile = new UserProfile(user, userInfo.getName() != null ? userInfo.getName() : "Google User", "");
                userProfile.updateProfileImageUrl(userInfo.getPicture());
                profileUpdated = true;
            } else if (!userInfo.getPicture().equals(userProfile.getProfileImageUrl())) {
                userProfile.updateProfileImageUrl(userInfo.getPicture());
                profileUpdated = true;
            }
        }

        // UserProfile 저장
        if (profileUpdated && userProfile != null) {
            userProfileRepository.save(userProfile);
        }

        // OAuth2 제공자 정보 업데이트
        if (user.getOauthProvider() == OAuthProvider.NATIVE) {
            user.setOauthProvider(OAuthProvider.GOOGLE);
            updated = true;
        }
        
        // 이메일 인증 상태 업데이트
        if (Boolean.TRUE.equals(userInfo.getEmailVerified()) && !user.isEmailVerified()) {
            user.setEmailVerified(true);
            updated = true;
        }
        
        // 업데이트 시간은 BaseEntity에서 자동 처리
        
        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        
        return userRepository.save(user);
    }
    
    private User createNewUser(OAuth2UserInfo userInfo) {
        return createNewUser(userInfo, UserType.GENERAL);
    }
    
    private User createNewUser(OAuth2UserInfo userInfo, UserType userType) {
        // OAuth 사용자 생성
        User user = new User(userInfo.getEmail(), userInfo.getId(), OAuthProvider.GOOGLE, userType);
        if (Boolean.TRUE.equals(userInfo.getEmailVerified())) {
            user.verifyEmail();
        }
        user.updateLastLogin();

        User savedUser = userRepository.save(user);

        // UserProfile 생성
        String userName = userInfo.getName() != null ? userInfo.getName() : "Google User";
        UserProfile userProfile = new UserProfile(savedUser, userName, "");
        if (userInfo.getPicture() != null) {
            userProfile.updateProfileImageUrl(userInfo.getPicture());
        }
        userProfileRepository.save(userProfile);

        return savedUser;
    }
    
    public String getGoogleAuthorizationUrl(String redirectUri, String state) {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + redirectUri +
                "&scope=openid%20profile%20email%20https://www.googleapis.com/auth/gmail.send" +
                "&response_type=code" +
                "&state=" + state;
    }
    
    public AuthenticationResponse handleGoogleCallback(String code, String state, String redirectUri) {
        try {
            // Step 1: Parse user type and action from state
            UserType userType = parseUserTypeFromState(state);
            String action = parseActionFromState(state);
            
            // Step 2: Exchange authorization code for access token
            Map<String, Object> tokenResponse = exchangeCodeForToken(code, redirectUri);
            
            // Step 3: Get user info using access token
            OAuth2UserInfo userInfo = getUserInfoFromGoogle((String) tokenResponse.get("access_token"));
            
            // Step 4: Handle login vs signup
            User user;
            if ("LOGIN".equals(action)) {
                // For login: find existing user by email first
                user = userRepository.findByEmailAndIsDeletedFalse(userInfo.getEmail())
                    .map(existingUser -> {
                        // Link OAuth to existing account if not already linked
                        if (existingUser.getOauthProvider() == OAuthProvider.NATIVE) {
                            existingUser.setOauthProvider(OAuthProvider.GOOGLE);
                            existingUser.setOauthId(userInfo.getId());
                        }

                        // Update profile info from OAuth if available
                        UserProfile existingProfile = userProfileRepository.findByUser(existingUser).orElse(null);
                        if (userInfo.getPicture() != null && (existingProfile == null || existingProfile.getProfileImageUrl() == null)) {
                            if (existingProfile == null) {
                                existingProfile = new UserProfile(existingUser, userInfo.getName() != null ? userInfo.getName() : "Google User", "");
                            }
                            existingProfile.updateProfileImageUrl(userInfo.getPicture());
                            userProfileRepository.save(existingProfile);
                        }
                        if (!existingUser.isEmailVerified()) {
                            existingUser.setEmailVerified(true);
                        }
                        existingUser.updateLastLogin();
                        return userRepository.save(existingUser);
                    })
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "계정이 없습니다. 회원가입을 먼저 진행해주세요."));
            } else {
                // For signup: check if user already exists by email
                if (userRepository.findByEmailAndIsDeletedFalse(userInfo.getEmail()).isPresent()) {
                    throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "이미 가입된 이메일입니다. 로그인을 진행해주세요.");
                }
                // Create new user with specified user type
                user = findOrCreateUser(userInfo, userType);
            }
            
            // Step 5: Generate JWT tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserResponseDto.from(user))
                    .build();
                    
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
    
    private Map<String, Object> exchangeCodeForToken(String code, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            
            String requestBody = "client_id=" + googleClientId +
                    "&client_secret=" + googleClientSecret +
                    "&code=" + code +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + redirectUri;
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    GOOGLE_TOKEN_URL, 
                    HttpMethod.POST, 
                    request, 
                    Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
    
    private OAuth2UserInfo getUserInfoFromGoogle(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    GOOGLE_USER_INFO_URL, 
                    HttpMethod.GET, 
                    request, 
                    Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
            }
            
            return new OAuth2UserInfo(response.getBody());
            
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
        }
    }
}