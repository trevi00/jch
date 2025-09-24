package org.jbd.backend.integration;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.service.UserService;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 통합 테스트")
class UserIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    private UserRegistrationDto userRegistrationDto;
    
    @BeforeEach
    void setUp() {
        userRegistrationDto = new UserRegistrationDto(
                "integration@test.com",
                "password123",
                "통합테스트사용자",
                UserType.GENERAL
        );
    }
    
    @Test
    @DisplayName("사용자 등록 및 조회 통합 테스트")
    void userRegistrationAndRetrievalTest() {
        // Given & When
        UserResponseDto savedUser = userService.registerUser(userRegistrationDto);
        
        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("integration@test.com");
        assertThat(savedUser.getName()).isEqualTo("통합테스트사용자");
        assertThat(savedUser.getUserType()).isEqualTo(UserType.GENERAL);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        
        // 데이터베이스에서 직접 조회하여 검증
        Optional<User> foundUser = userRepository.findByEmail("integration@test.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("통합테스트사용자");
        assertThat(foundUser.get().getUserType()).isEqualTo(UserType.GENERAL);
        
        // Service를 통한 조회 검증
        UserResponseDto retrievedUser = userService.getUserByEmail("integration@test.com");
        assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(retrievedUser.getName()).isEqualTo(savedUser.getName());
    }
    
    @Test
    @DisplayName("사용자 통계 정보 통합 테스트")
    void userStatisticsTest() {
        // Given - 다양한 유형의 사용자들 생성
        createTestUsers();
        
        // When
        long generalUsers = userService.countUsersByType(UserType.GENERAL);
        long companyUsers = userService.countUsersByType(UserType.COMPANY);
        long adminUsers = userService.countUsersByType(UserType.ADMIN);
        
        // Then
        assertThat(generalUsers).isEqualTo(2);
        assertThat(companyUsers).isEqualTo(1);
        assertThat(adminUsers).isEqualTo(1);
    }
    
    @Test
    @DisplayName("사용자 유형별 조회 통합 테스트")
    void usersByTypeTest() {
        // Given
        createTestUsers();
        
        // When
        List<UserResponseDto> generalUsers = userService.getUsersByType(UserType.GENERAL);
        List<UserResponseDto> companyUsers = userService.getUsersByType(UserType.COMPANY);
        List<UserResponseDto> adminUsers = userService.getUsersByType(UserType.ADMIN);
        
        // Then
        assertThat(generalUsers).hasSize(2);
        assertThat(companyUsers).hasSize(1);
        assertThat(adminUsers).hasSize(1);
        
        // 일반 사용자들 검증
        assertThat(generalUsers).extracting(UserResponseDto::getName)
                .containsExactlyInAnyOrder("일반테스트사용자1", "일반테스트사용자2");
        
        // 기업 사용자 검증
        assertThat(companyUsers.get(0).getName()).isEqualTo("기업테스트사용자");
        assertThat(companyUsers.get(0).getUserType()).isEqualTo(UserType.COMPANY);
        
        // 관리자 검증
        assertThat(adminUsers.get(0).getName()).isEqualTo("관리자테스트사용자");
        assertThat(adminUsers.get(0).getUserType()).isEqualTo(UserType.ADMIN);
    }
    
    @Test
    @DisplayName("최근 가입 사용자 통계 통합 테스트")
    void newUsersStatisticsTest() {
        // Given
        createTestUsers();
        
        // When - 오늘부터의 신규 사용자 수 조회
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long newGeneralUsers = userService.countNewUsersByTypeFromDate(UserType.GENERAL, today);
        long newCompanyUsers = userService.countNewUsersByTypeFromDate(UserType.COMPANY, today);
        long newAdminUsers = userService.countNewUsersByTypeFromDate(UserType.ADMIN, today);
        
        // Then
        assertThat(newGeneralUsers).isEqualTo(2);
        assertThat(newCompanyUsers).isEqualTo(1);
        assertThat(newAdminUsers).isEqualTo(1);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회 통합 테스트")
    void getUserByIdTest() {
        // Given
        UserResponseDto savedUser = userService.registerUser(userRegistrationDto);
        
        // When
        UserResponseDto retrievedUser = userService.getUserById(savedUser.getId());
        
        // Then
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(retrievedUser.getName()).isEqualTo(savedUser.getName());
        assertThat(retrievedUser.getUserType()).isEqualTo(savedUser.getUserType());
    }
    
    @Test
    @DisplayName("OAuth 사용자 등록 통합 테스트")
    void oauthUserRegistrationTest() {
        // Given
        String email = "oauth@test.com";
        String name = "OAuth테스트사용자";
        String provider = "google";
        String oauthId = "google123";
        
        // When
        UserResponseDto oauthUser = userService.registerOAuthUser(email, name, provider, oauthId);
        
        // Then
        assertThat(oauthUser).isNotNull();
        assertThat(oauthUser.getEmail()).isEqualTo(email);
        assertThat(oauthUser.getName()).isEqualTo(name);
        assertThat(oauthUser.getUserType()).isEqualTo(UserType.GENERAL);
        
        // 데이터베이스에서 직접 확인
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
        assertThat(foundUser.get().getName()).isEqualTo(name);
        assertThat(foundUser.get().isOAuthUser()).isTrue();
    }
    
    @Test
    @DisplayName("관리자 권한 변환 통합 테스트")
    void convertToAdminTest() {
        // Given
        UserResponseDto savedUser = userService.registerUser(userRegistrationDto);
        
        // When
        userService.convertToAdmin(savedUser.getId());
        
        // Then
        UserResponseDto adminUser = userService.getUserById(savedUser.getId());
        assertThat(adminUser.getUserType()).isEqualTo(UserType.ADMIN);
        
        // 데이터베이스에서 직접 확인
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserType()).isEqualTo(UserType.ADMIN);
    }
    
    private void createTestUsers() {
        // GENERAL 사용자 2명
        userRepository.save(new User(
                "general1@test.com",
                "password",
                "일반테스트사용자1",
                UserType.GENERAL
        ));
        
        userRepository.save(new User(
                "general2@test.com",
                "password",
                "일반테스트사용자2",
                UserType.GENERAL
        ));
        
        // COMPANY 사용자 1명
        userRepository.save(new User(
                "company@test.com",
                "password",
                "기업테스트사용자",
                UserType.COMPANY
        ));
        
        // ADMIN 사용자 1명
        userRepository.save(new User(
                "admin@test.com",
                "password",
                "관리자테스트사용자",
                UserType.ADMIN
        ));
    }
}