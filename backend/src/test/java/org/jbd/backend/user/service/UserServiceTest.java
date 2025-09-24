package org.jbd.backend.user.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.dto.UserUpdateDto;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailVerificationService emailVerificationService;
    
    @InjectMocks
    private UserService userService;
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                .build();
    }
    
    @Test
    @DisplayName("사용자 등록 성공")
    void registerUser_Success() {
        // given
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test@example.com",
                "password123",
                "테스트사용자",
                UserType.GENERAL
        );
        
        String encodedPassword = "encodedPassword";
        User savedUser = new User(registrationDto.getEmail(), encodedPassword, registrationDto.getName(), registrationDto.getUserType());
        
        when(userRepository.existsByEmailAndIsDeletedFalse(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // when
        UserResponseDto result = userService.registerUser(registrationDto);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(registrationDto.getEmail());
        assertThat(result.getName()).isEqualTo(registrationDto.getName());
        assertThat(result.getUserType()).isEqualTo(registrationDto.getUserType());
        
        verify(userRepository).existsByEmailAndIsDeletedFalse(registrationDto.getEmail());
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).sendVerificationEmail(eq(registrationDto.getEmail()), anyString());
    }
    
    @Test
    @DisplayName("사용자 등록 실패 - 이메일 중복")
    void registerUser_EmailAlreadyExists() {
        // given
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test@example.com",
                "password123",
                "테스트사용자"
        );
        
        when(userRepository.existsByEmailAndIsDeletedFalse(registrationDto.getEmail())).thenReturn(true);
        
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.registerUser(registrationDto));
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
        
        verify(userRepository).existsByEmailAndIsDeletedFalse(registrationDto.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("OAuth 사용자 등록 성공")
    void registerOAuthUser_Success() {
        // given
        String email = "oauth@example.com";
        String name = "OAuth사용자";
        String oauthProvider = "google";
        String oauthId = "12345";
        
        User savedUser = new User(email, name, oauthProvider, oauthId, UserType.GENERAL);
        
        when(userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId)).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndIsDeletedFalse(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // when
        UserResponseDto result = userService.registerOAuthUser(email, name, oauthProvider, oauthId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getOauthProvider()).isEqualTo(oauthProvider);
        
        verify(userRepository).findByOauthProviderAndOauthId(oauthProvider, oauthId);
        verify(userRepository).findByEmailAndIsDeletedFalse(email);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("기존 OAuth 사용자 로그인")
    void registerOAuthUser_ExistingUser() {
        // given
        String email = "oauth@example.com";
        String name = "OAuth사용자";
        String oauthProvider = "google";
        String oauthId = "12345";
        
        User existingUser = new User(email, name, oauthProvider, oauthId, UserType.GENERAL);
        
        when(userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        
        // when
        UserResponseDto result = userService.registerOAuthUser(email, name, oauthProvider, oauthId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        
        verify(userRepository).findByOauthProviderAndOauthId(oauthProvider, oauthId);
        verify(userRepository).save(existingUser);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 성공")
    void getUserById_Success() {
        // given
        Long userId = 1L;
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // when
        UserResponseDto result = userService.getUserById(userId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getName()).isEqualTo(user.getName());
        
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 실패 - 사용자 없음")
    void getUserById_NotFound() {
        // given
        Long userId = 1L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.getUserById(userId));
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 정보 업데이트 성공")
    void updateUser_Success() {
        // given
        Long userId = 1L;
        User existingUser = new User("test@example.com", "password", "기존사용자", UserType.GENERAL);
        
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("수정된사용자");
        updateDto.setNickname("새닉네임");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByNickname(updateDto.getNickname())).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        
        // when
        UserResponseDto result = userService.updateUser(userId, updateDto);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateDto.getName());
        
        verify(userRepository).findById(userId);
        verify(userRepository).existsByNickname(updateDto.getNickname());
        verify(userRepository).save(existingUser);
    }
    
    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser_Success() {
        // given
        Long userId = 1L;
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        
        // when
        userService.deleteUser(userId);
        
        // then
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        assertTrue(user.isDeleted());
    }
    
    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() {
        // given
        String token = "verification-token";
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        user.setEmailVerificationToken(token, LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        
        // when
        userService.verifyEmail(token);
        
        // then
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository).save(user);
        assertTrue(user.getEmailVerified());
    }
    
    @Test
    @DisplayName("이메일 인증 실패 - 만료된 토큰")
    void verifyEmail_ExpiredToken() {
        // given
        String token = "expired-token";
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        user.setEmailVerificationToken(token, LocalDateTime.now().minusHours(1));
        
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));
        
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.verifyEmail(token));
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
        
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("회사 이메일 인증 요청 성공")
    void requestCompanyEmailVerification_Success() {
        // given
        Long userId = 1L;
        String companyEmail = "company@example.com";
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByCompanyEmail(companyEmail)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        
        // when
        userService.requestCompanyEmailVerification(userId, companyEmail);
        
        // then
        verify(userRepository).findById(userId);
        verify(userRepository).existsByCompanyEmail(companyEmail);
        verify(userRepository).save(user);
        verify(emailVerificationService).sendCompanyVerificationEmail(eq(companyEmail), anyString());
        
        assertThat(user.getCompanyEmail()).isEqualTo(companyEmail);
    }
    
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // given
        Long userId = 1L;
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";
        String encodedCurrentPassword = "encodedCurrentPassword";
        String encodedNewPassword = "encodedNewPassword";
        
        User user = new User("test@example.com", encodedCurrentPassword, "테스트사용자", UserType.GENERAL);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(user)).thenReturn(user);
        
        // when
        userService.changePassword(userId, currentPassword, newPassword);
        
        // then
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }
    
    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 현재 비밀번호")
    void changePassword_InvalidCurrentPassword() {
        // given
        Long userId = 1L;
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";
        
        User user = new User("test@example.com", encodedPassword, "테스트사용자", UserType.GENERAL);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, encodedPassword)).thenReturn(false);
        
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.changePassword(userId, currentPassword, newPassword));
        
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
        
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 타입별 개수 조회")
    void countUsersByType_Success() {
        // given
        UserType userType = UserType.GENERAL;
        long expectedCount = 10L;
        
        when(userRepository.countActiveUsersByType(userType)).thenReturn(expectedCount);
        
        // when
        long result = userService.countUsersByType(userType);
        
        // then
        assertThat(result).isEqualTo(expectedCount);
        verify(userRepository).countActiveUsersByType(userType);
    }
    
    @Test
    @DisplayName("만료된 토큰 정리")
    void cleanupExpiredTokens_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        User expiredEmailUser = new User("expired1@example.com", "password", "사용자1", UserType.GENERAL);
        User expiredCompanyEmailUser = new User("expired2@example.com", "password", "사용자2", UserType.GENERAL);
        User expiredLockedUser = new User("expired3@example.com", "password", "사용자3", UserType.GENERAL);
        
        when(userRepository.findUsersWithExpiredEmailVerification(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredEmailUser));
        when(userRepository.findUsersWithExpiredCompanyEmailVerification(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredCompanyEmailUser));
        when(userRepository.findUsersWithExpiredAccountLock(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredLockedUser));
        
        // when
        userService.cleanupExpiredTokens();
        
        // then
        verify(userRepository).findUsersWithExpiredEmailVerification(any(LocalDateTime.class));
        verify(userRepository).findUsersWithExpiredCompanyEmailVerification(any(LocalDateTime.class));
        verify(userRepository).findUsersWithExpiredAccountLock(any(LocalDateTime.class));
        verify(userRepository, times(3)).saveAll(anyList());
    }
}