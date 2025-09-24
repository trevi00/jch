package org.jbd.backend.auth.service;

import org.jbd.backend.auth.dto.OAuth2UserInfo;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.domain.enums.OAuthProvider;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.repository.UserProfileRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public CustomOAuth2UserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        OAuth2UserInfo userInfo = new OAuth2UserInfo(oauth2User.getAttributes());
        String email = userInfo.getEmail();
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .map(existingUser -> updateExistingUser(existingUser, userInfo))
                .orElseGet(() -> registerNewUser(userInfo));
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    private User registerNewUser(OAuth2UserInfo userInfo) {
        // OAuth 사용자 생성
        User user = new User(userInfo.getEmail(), userInfo.getId(), OAuthProvider.GOOGLE, UserType.GENERAL);
        if (Boolean.TRUE.equals(userInfo.getEmailVerified())) {
            user.verifyEmail();
        }

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
    
    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
        boolean userUpdated = false;
        boolean profileUpdated = false;

        // UserProfile 조회 및 업데이트
        UserProfile userProfile = userProfileRepository.findByUser(existingUser).orElse(null);

        // 이름 업데이트 (UserProfile에서)
        if (userInfo.getName() != null) {
            if (userProfile == null) {
                userProfile = new UserProfile(existingUser, userInfo.getName(), "");
                profileUpdated = true;
            } else if (!userInfo.getName().equals(userProfile.getFullName())) {
                userProfile.updateName(userInfo.getName(), "");
                profileUpdated = true;
            }
        }

        // 프로필 이미지 업데이트
        if (userInfo.getPicture() != null) {
            if (userProfile == null) {
                userProfile = new UserProfile(existingUser, userInfo.getName() != null ? userInfo.getName() : "Google User", "");
                userProfile.updateProfileImageUrl(userInfo.getPicture());
                profileUpdated = true;
            } else if (!userInfo.getPicture().equals(userProfile.getProfileImageUrl())) {
                userProfile.updateProfileImageUrl(userInfo.getPicture());
                profileUpdated = true;
            }
        }

        // OAuth 제공자 업데이트
        if (existingUser.getOauthProvider() == OAuthProvider.NATIVE) {
            existingUser.setOauthProvider(OAuthProvider.GOOGLE);
            userUpdated = true;
        }

        // 이메일 인증 업데이트
        if (Boolean.TRUE.equals(userInfo.getEmailVerified()) && !existingUser.isEmailVerified()) {
            existingUser.verifyEmail();
            userUpdated = true;
        }

        // 업데이트 저장
        if (profileUpdated && userProfile != null) {
            userProfileRepository.save(userProfile);
        }

        if (userUpdated) {
            return userRepository.save(existingUser);
        }

        return existingUser;
    }
}