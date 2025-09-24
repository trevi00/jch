package org.jbd.backend.user.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.domain.enums.Gender;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                .build();
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        String email = "test@example.com";
        User user = new User(email, "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findByEmail(email);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
        assertThat(foundUser.get().getName()).isEqualTo("테스트사용자");
    }
    
    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 시 빈 Optional 반환")
    void findByEmail_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        
        // then
        assertThat(foundUser).isEmpty();
    }
    
    @Test
    @DisplayName("이메일로 삭제되지 않은 사용자 조회 성공")
    void findByEmailAndIsDeletedFalse_Success() {
        // given
        String email = "test@example.com";
        User user = new User(email, "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findByEmailAndIsDeletedFalse(email);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }
    
    @Test
    @DisplayName("삭제된 사용자는 findByEmailAndIsDeletedFalse로 조회되지 않음")
    void findByEmailAndIsDeletedFalse_DeletedUserNotFound() {
        // given
        String email = "test@example.com";
        User user = new User(email, "password", "테스트사용자", UserType.GENERAL);
        user.delete();
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findByEmailAndIsDeletedFalse(email);
        
        // then
        assertThat(foundUser).isEmpty();
    }
    
    @Test
    @DisplayName("OAuth 제공자와 ID로 사용자 조회 성공")
    void findByOauthProviderAndOauthId_Success() {
        // given
        String email = "oauth@example.com";
        String provider = "google";
        String oauthId = "12345";
        User user = new User(email, "OAuth사용자", provider, oauthId, UserType.GENERAL);
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getOauthProvider()).isEqualTo(provider);
        assertThat(foundUser.get().getOauthId()).isEqualTo(oauthId);
    }
    
    @Test
    @DisplayName("이메일 인증 토큰으로 사용자 조회 성공")
    void findByEmailVerificationToken_Success() {
        // given
        String email = "test@example.com";
        String token = "verification-token";
        User user = new User(email, "password", "테스트사용자", UserType.GENERAL);
        user.setEmailVerificationToken(token, LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findByEmailVerificationToken(token);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmailVerificationToken()).isEqualTo(token);
    }
    
    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재함")
    void existsByEmail_True() {
        // given
        String email = "test@example.com";
        User user = new User(email, "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);
        
        // when
        boolean exists = userRepository.existsByEmail(email);
        
        // then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
    void existsByEmail_False() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        
        // then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("삭제되지 않은 이메일 존재 여부 확인 - 삭제된 사용자는 false")
    void existsByEmailAndIsDeletedFalse_DeletedUser() {
        // given
        String email = "deleted@example.com";
        User user = new User(email, "password", "삭제된사용자", UserType.GENERAL);
        user.delete();
        entityManager.persistAndFlush(user);
        
        // when
        boolean exists = userRepository.existsByEmailAndIsDeletedFalse(email);
        
        // then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("닉네임 존재 여부 확인")
    void existsByNickname_True() {
        // given
        String nickname = "테스트닉네임";
        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        user.updateBasicInfo("테스트사용자", nickname, null, null, null, null);
        entityManager.persistAndFlush(user);
        
        // when
        boolean exists = userRepository.existsByNickname(nickname);
        
        // then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("사용자 타입별 조회")
    void findByUserType() {
        // given
        User generalUser = new User("general@example.com", "password", "일반사용자", UserType.GENERAL);
        User companyUser = new User("company@example.com", "password", "기업사용자", UserType.COMPANY);
        User adminUser = new User("admin@example.com", "password", "관리자", UserType.ADMIN);
        
        entityManager.persist(generalUser);
        entityManager.persist(companyUser);
        entityManager.persist(adminUser);
        entityManager.flush();
        
        // when
        List<User> generalUsers = userRepository.findByUserType(UserType.GENERAL);
        List<User> companyUsers = userRepository.findByUserType(UserType.COMPANY);
        List<User> adminUsers = userRepository.findByUserType(UserType.ADMIN);
        
        // then
        assertThat(generalUsers).hasSize(1);
        assertThat(companyUsers).hasSize(1);
        assertThat(adminUsers).hasSize(1);
        
        assertThat(generalUsers.get(0).getUserType()).isEqualTo(UserType.GENERAL);
        assertThat(companyUsers.get(0).getUserType()).isEqualTo(UserType.COMPANY);
        assertThat(adminUsers.get(0).getUserType()).isEqualTo(UserType.ADMIN);
    }
    
    @Test
    @DisplayName("활성 사용자 타입별 조회")
    void findActiveUsersByType() {
        // given
        User activeUser = new User("active@example.com", "password", "활성사용자", UserType.GENERAL);
        User deletedUser = new User("deleted@example.com", "password", "삭제된사용자", UserType.GENERAL);
        deletedUser.delete();
        
        entityManager.persist(activeUser);
        entityManager.persist(deletedUser);
        entityManager.flush();
        
        // when
        List<User> activeUsers = userRepository.findActiveUsersByType(UserType.GENERAL);
        
        // then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("active@example.com");
    }
    
    @Test
    @DisplayName("활성 사용자 타입별 개수 조회")
    void countActiveUsersByType() {
        // given
        User activeUser1 = new User("active1@example.com", "password", "활성사용자1", UserType.GENERAL);
        User activeUser2 = new User("active2@example.com", "password", "활성사용자2", UserType.GENERAL);
        User deletedUser = new User("deleted@example.com", "password", "삭제된사용자", UserType.GENERAL);
        User companyUser = new User("company@example.com", "password", "기업사용자", UserType.COMPANY);
        
        deletedUser.delete();
        
        entityManager.persist(activeUser1);
        entityManager.persist(activeUser2);
        entityManager.persist(deletedUser);
        entityManager.persist(companyUser);
        entityManager.flush();
        
        // when
        long generalCount = userRepository.countActiveUsersByType(UserType.GENERAL);
        long companyCount = userRepository.countActiveUsersByType(UserType.COMPANY);
        
        // then
        assertThat(generalCount).isEqualTo(2);
        assertThat(companyCount).isEqualTo(1);
    }
    
    @Test
    @DisplayName("특정 날짜 이후 신규 사용자 개수 조회")
    void countNewUsersByTypeFromDate() {
        // given
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        User oldUser = new User("old@example.com", "password", "오래된사용자", UserType.GENERAL);
        User newUser = new User("new@example.com", "password", "신규사용자", UserType.GENERAL);
        
        entityManager.persist(oldUser);
        entityManager.flush();
        
        entityManager.persist(newUser);
        entityManager.flush();
        
        // when
        long newUsersCount = userRepository.countNewUsersByTypeFromDate(UserType.GENERAL, oneDayAgo);
        
        // then
        assertThat(newUsersCount).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    @DisplayName("만료된 이메일 인증 사용자 조회")
    void findUsersWithExpiredEmailVerification() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusHours(1);
        LocalDateTime futureTime = now.plusHours(1);
        
        User expiredUser = new User("expired@example.com", "password", "만료된사용자", UserType.GENERAL);
        User validUser = new User("valid@example.com", "password", "유효한사용자", UserType.GENERAL);
        
        expiredUser.setEmailVerificationToken("expired-token", pastTime);
        validUser.setEmailVerificationToken("valid-token", futureTime);
        
        entityManager.persist(expiredUser);
        entityManager.persist(validUser);
        entityManager.flush();
        
        // when
        List<User> expiredUsers = userRepository.findUsersWithExpiredEmailVerification(now);
        
        // then
        assertThat(expiredUsers).hasSize(1);
        assertThat(expiredUsers.get(0).getEmail()).isEqualTo("expired@example.com");
    }
    
    @Test
    @DisplayName("계정 잠김이 만료된 사용자 조회")
    void findUsersWithExpiredAccountLock() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusHours(1);
        LocalDateTime futureTime = now.plusHours(1);
        
        User expiredLockUser = new User("expired@example.com", "password", "만료된잠김사용자", UserType.GENERAL);
        User validLockUser = new User("valid@example.com", "password", "유효한잠김사용자", UserType.GENERAL);
        
        expiredLockUser.lockAccount(pastTime);
        validLockUser.lockAccount(futureTime);
        
        entityManager.persist(expiredLockUser);
        entityManager.persist(validLockUser);
        entityManager.flush();
        
        // when
        List<User> expiredLockUsers = userRepository.findUsersWithExpiredAccountLock(now);
        
        // then
        assertThat(expiredLockUsers).hasSize(1);
        assertThat(expiredLockUsers.get(0).getEmail()).isEqualTo("expired@example.com");
    }
    
    @Test
    @DisplayName("회사 이메일이 인증된 사용자 조회")
    void findUsersWithVerifiedCompanyEmail() {
        // given
        User verifiedUser = new User("verified@example.com", "password", "인증된사용자", UserType.GENERAL);
        User unverifiedUser = new User("unverified@example.com", "password", "미인증사용자", UserType.GENERAL);
        
        verifiedUser.setCompanyEmailVerification("company@example.com", "token", LocalDateTime.now().plusHours(1));
        verifiedUser.verifyCompanyEmail();
        
        unverifiedUser.setCompanyEmailVerification("company2@example.com", "token2", LocalDateTime.now().plusHours(1));
        
        entityManager.persist(verifiedUser);
        entityManager.persist(unverifiedUser);
        entityManager.flush();
        
        // when
        List<User> verifiedUsers = userRepository.findUsersWithVerifiedCompanyEmail(UserType.GENERAL);
        
        // then
        assertThat(verifiedUsers).hasSize(1);
        assertThat(verifiedUsers.get(0).getEmail()).isEqualTo("verified@example.com");
        assertThat(verifiedUsers.get(0).getCompanyEmailVerified()).isTrue();
    }
}