package org.jbd.backend.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.domain.enums.OAuthProvider;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 엔티티
 *
 * 시스템의 핵심 도메인 객체로, 모든 사용자 정보와 인증 데이터를 관리합니다.
 * 다양한 사용자 타입(일반, 기업, 관리자)과 인증 방식(일반, OAuth2)을 지원합니다.
 *
 * 도메인 관계:
 * - UserProfile (1:1): 상세한 프로필 정보
 * - CareerHistory (1:N): 경력 이력
 * - Education (1:N): 학력 정보
 * - Certification (1:N): 자격증 정보
 * - Portfolio (1:N): 포트폴리오
 * - Post (1:N): 작성한 게시글
 * - Comment (1:N): 작성한 댓글
 * - JobPosting (1:N): 등록한 채용공고 (기업 사용자)
 * - JobApplication (1:N): 지원한 채용공고 (일반 사용자)
 *
 * 비즈니스 규칙:
 * - 이메일은 유일해야 함 (유니크 제약조건)
 * - OAuth 사용자는 이메일 인증이 자동으로 완료됨
 * - 기업 사용자로 전환하려면 기업 이메일 인증이 필요함
 * - 관리자는 별도의 변환 시점을 기록함
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see UserType
 * @see OAuthProvider
 * @see EmploymentStatus
 * @see BaseEntity
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType = UserType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    private OAuthProvider oauthProvider = OAuthProvider.NATIVE;

    @Column(name = "oauth_id")
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus = EmploymentStatus.JOB_SEEKING;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "company_email_verified", nullable = false)
    private Boolean companyEmailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "admin_converted_at")
    private LocalDateTime adminConvertedAt;

    public User() {}

    public User(String email, String passwordHash, UserType userType) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
        this.oauthProvider = OAuthProvider.NATIVE;
    }

    public User(String email, String oauthId, OAuthProvider oauthProvider, UserType userType) {
        this.email = email;
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
        this.userType = userType;
        this.emailVerified = true; // OAuth users are pre-verified
    }

    // Business methods
    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void verifyCompanyEmail() {
        this.companyEmailVerified = true;
    }

    public void convertToCompanyUser() {
        if (!companyEmailVerified) {
            throw new IllegalStateException("Company email must be verified before conversion");
        }
        this.userType = UserType.COMPANY;
    }

    public void convertToAdmin() {
        this.userType = UserType.ADMIN;
        this.adminConvertedAt = LocalDateTime.now();
    }


    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void updateEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public boolean isOAuthUser() {
        return oauthProvider != OAuthProvider.NATIVE && oauthId != null;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isCompanyUser() {
        return userType == UserType.COMPANY;
    }

    public boolean isGeneralUser() {
        return userType == UserType.GENERAL;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isCompanyEmailVerified() {
        return Boolean.TRUE.equals(companyEmailVerified);
    }

    public String getPassword() {
        return passwordHash;
    }

    public boolean isAccountLocked() {
        return false; // 현재 스키마에는 계정 잠금 기능이 없음
    }

    /**
     * 임시 getName() 메서드 - UserProfile과의 순환 의존성을 피하기 위해
     * 이메일의 로컬 부분을 반환합니다.
     * TODO: 서비스 레이어에서 UserProfile을 조회하여 실제 이름을 반환하도록 개선 필요
     */
    public String getName() {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "Unknown User";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public OAuthProvider getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(OAuthProvider oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getCompanyEmailVerified() {
        return companyEmailVerified;
    }

    public void setCompanyEmailVerified(Boolean companyEmailVerified) {
        this.companyEmailVerified = companyEmailVerified;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getAdminConvertedAt() {
        return adminConvertedAt;
    }

    public void setAdminConvertedAt(LocalDateTime adminConvertedAt) {
        this.adminConvertedAt = adminConvertedAt;
    }
}