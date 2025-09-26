package org.jbd.backend.user.service;

import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.domain.enums.OAuthProvider;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 관리 서비스
 *
 * 사용자 등록, 인증, 프로필 관리, 이메일 인증, 권한 관리 등 사용자와 관련된
 * 모든 비즈니스 로직을 처리합니다. 일반 사용자와 기업 사용자, OAuth 인증 사용자를
 * 지원하며, 사용자 생명주기 전반에 걸친 기능을 제공합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see User
 * @see UserProfile
 * @see UserResponseDto
 * @see UserRegistrationDto
 */
@Service
@Transactional
public class UserService {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /** 사용자 데이터 액세스 리포지토리 */
    private final UserRepository userRepository;

    /** 사용자 프로필 데이터 액세스 리포지토리 */
    private final UserProfileRepository userProfileRepository;

    /** 비밀번호 암호화 서비스 */
    private final PasswordEncoder passwordEncoder;

    /** 이메일 인증 관리 서비스 */
    private final EmailVerificationService emailVerificationService;

    /**
     * UserService 생성자
     *
     * @param userRepository 사용자 리포지토리
     * @param userProfileRepository 사용자 프로필 리포지토리
     * @param passwordEncoder 비밀번호 암호화 서비스
     * @param emailVerificationService 이메일 인증 서비스
     */
    public UserService(UserRepository userRepository,
                      UserProfileRepository userProfileRepository,
                      PasswordEncoder passwordEncoder,
                      EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 새로운 사용자를 등록합니다.
     * 이메일 중복 검사, 비밀번호 암호화, 기본 프로필 생성 등을 수행합니다.
     * 일반 사용자와 기업 사용자 모두 지원합니다.
     *
     * @param registrationDto 사용자 등록 정보
     *                       - email: 이메일 주소 (필수, 유일해야 함)
     *                       - password: 비밀번호 (필수)
     *                       - name: 사용자 이름 (선택)
     *                       - userType: 사용자 유형 (GENERAL, COMPANY)
     * @return UserResponseDto 등록된 사용자 정보
     * @throws BusinessException 이메일 중복 시 ErrorCode.USER_ALREADY_EXISTS
     * @see UserRegistrationDto
     * @see UserResponseDto
     */
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // 이메일 중복 확인
        if (userRepository.existsByEmailAndIsDeletedFalse(registrationDto.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());

        // 사용자 생성
        User user = new User(
                registrationDto.getEmail(),
                encodedPassword,
                registrationDto.getUserType()
        );

        User savedUser = userRepository.save(user);

        // 기본 프로필 생성 (name이 있는 경우)
        UserProfile userProfile = null;
        if (registrationDto.getName() != null && !registrationDto.getName().trim().isEmpty()) {
            userProfile = new UserProfile(savedUser, registrationDto.getName(), "");
            userProfileRepository.save(userProfile);
        }

        return UserResponseDto.from(savedUser, userProfile);
    }

    /**
     * OAuth 인증을 통한 사용자를 등록합니다.
     * Google, Kakao 등 OAuth 제공업체를 통해 인증된 사용자를 등록하거나 기존 사용자 정보를 반환합니다.
     * 이미 등록된 OAuth 사용자인 경우 기존 정보를 반환합니다.
     *
     * @param email 사용자 이메일 주소
     * @param oauthProvider OAuth 제공업체 (GOOGLE, KAKAO 등)
     * @param oauthId OAuth 제공업체에서 제공하는 고유 사용자 ID
     * @param userType 사용자 유형 (GENERAL, COMPANY)
     * @return UserResponseDto 등록된 또는 기존 OAuth 사용자 정보
     * @see OAuthProvider
     * @see UserType
     * @see UserResponseDto
     */
    public UserResponseDto registerOAuthUser(String email, OAuthProvider oauthProvider, String oauthId, UserType userType) {
        // OAuth 사용자 중복 확인
        Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
            return UserResponseDto.from(user, profile);
        }

        // 이메일로 기존 계정 확인
        Optional<User> existingEmailUser = userRepository.findByEmailAndIsDeletedFalse(email);
        if (existingEmailUser.isPresent()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // OAuth 사용자 생성
        User user = new User(email, oauthId, oauthProvider, userType);
        User savedUser = userRepository.save(user);

        return UserResponseDto.from(savedUser, null);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        return UserResponseDto.from(user, profile);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        return UserResponseDto.from(user, profile);
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

    public UserResponseDto updateEmploymentStatus(Long userId, EmploymentStatus employmentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.updateEmploymentStatus(employmentStatus);
        User savedUser = userRepository.save(user);
        UserProfile profile = userProfileRepository.findByUser(savedUser).orElse(null);
        return UserResponseDto.from(savedUser, profile);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.deactivate();
        userRepository.save(user);
    }

    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        user.verifyEmail();
        userRepository.save(user);
    }

    public void verifyCompanyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isCompanyEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED, "이미 인증된 회사 이메일입니다.");
        }

        user.verifyCompanyEmail();
        userRepository.save(user);
    }

    public void convertToCompanyUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isCompanyEmailVerified()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "회사 이메일 인증이 필요합니다.");
        }

        if (user.getUserType() == UserType.COMPANY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 기업 유저입니다.");
        }

        user.convertToCompanyUser();
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive) // 활성 사용자만 반환
                .map(user -> {
                    UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
                    return UserResponseDto.from(user, profile);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByType(UserType userType) {
        return userRepository.findActiveUsersByType(userType)
                .stream()
                .map(user -> {
                    UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
                    return UserResponseDto.from(user, profile);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUsersByType(UserType userType) {
        return userRepository.countActiveUsersByType(userType);
    }

    @Transactional(readOnly = true)
    public long countNewUsersByTypeFromDate(UserType userType, LocalDateTime fromDate) {
        return userRepository.countNewUsersByTypeFromDate(userType, fromDate);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isOAuthUser()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "OAuth 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedNewPassword);

        userRepository.save(user);
    }

    public UserResponseDto updateUser(Long userId, org.jbd.backend.user.dto.UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // User 정보 업데이트 (고용 상태만)
        if (updateDto.getEmploymentStatus() != null) {
            user.updateEmploymentStatus(updateDto.getEmploymentStatus());
            userRepository.save(user);
        }

        // UserProfile 조회 및 업데이트
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);

        if (profile == null && hasProfileData(updateDto)) {
            // 프로필이 없고 업데이트할 프로필 데이터가 있으면 새로 생성
            profile = new UserProfile(user,
                updateDto.getName() != null ? updateDto.getName() : "", "");
            updateProfileFromDto(profile, updateDto);
            userProfileRepository.save(profile);
        } else if (profile != null) {
            // 기존 프로필 업데이트
            updateProfileFromDto(profile, updateDto);
            userProfileRepository.save(profile);
        }

        return UserResponseDto.from(user, profile);
    }

    private boolean hasProfileData(org.jbd.backend.user.dto.UserUpdateDto updateDto) {
        return updateDto.getName() != null || updateDto.getNickname() != null ||
               updateDto.getPhoneNumber() != null || updateDto.getAge() != null ||
               updateDto.getGender() != null || updateDto.getResidenceRegion() != null ||
               updateDto.getDesiredJob() != null || updateDto.getDesiredCompany() != null;
    }

    private void updateProfileFromDto(UserProfile profile, org.jbd.backend.user.dto.UserUpdateDto updateDto) {
        if (updateDto.getName() != null) {
            profile.updateName(updateDto.getName(), profile.getLastName() != null ? profile.getLastName() : "");
        }
        if (updateDto.getPhoneNumber() != null || updateDto.getResidenceRegion() != null) {
            profile.updateContactInfo(
                updateDto.getPhoneNumber() != null ? updateDto.getPhoneNumber() : profile.getPhoneNumber(),
                updateDto.getResidenceRegion() != null ? updateDto.getResidenceRegion() : profile.getLocation()
            );
        }
        if (updateDto.getAge() != null || updateDto.getGender() != null) {
            profile.updateBasicInfo(
                profile.getFirstName(),
                profile.getLastName(),
                updateDto.getAge() != null ? updateDto.getAge() : profile.getAge(),
                updateDto.getGender() != null ? updateDto.getGender() : profile.getGender()
            );
        }
        if (updateDto.getDesiredJob() != null) {
            profile.updateJobPreference(updateDto.getDesiredJob());
        }
        // desiredCompany는 현재 UserProfile에 필드가 없으므로 스킵
        // 추후 필요시 UserProfile에 desiredCompany 필드 추가 가능
    }

    public void verifyEmail(String token) {
        if (!emailVerificationService.verifyToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않거나 만료된 토큰입니다.");
        }

        logger.info("이메일 인증이 완료되었습니다: {}", token);
    }

    public void requestCompanyEmailVerification(Long userId, String companyEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 이미 인증된 경우 체크
        if (user.isCompanyEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED, "이미 회사 이메일이 인증되었습니다.");
        }

        // 회사 이메일 도메인 검증 (간단한 예시)
        if (!isValidCompanyEmail(companyEmail)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 회사 이메일입니다.");
        }

        String token = emailVerificationService.generateToken();
        emailVerificationService.sendCompanyVerificationEmail(companyEmail, token);

        logger.info("회사 이메일 인증 메일 발송 완료: {}", companyEmail);
    }

    private boolean isValidCompanyEmail(String email) {
        // 간단한 회사 이메일 검증 로직
        // 실제로는 더 정교한 도메인 검증이 필요
        return email != null && email.contains("@") &&
               !email.endsWith("@gmail.com") &&
               !email.endsWith("@naver.com") &&
               !email.endsWith("@kakao.com") &&
               !email.endsWith("@yahoo.com");
    }

    public void verifyCompanyEmail(String token) {
        if (!emailVerificationService.verifyToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않거나 만료된 회사 이메일 인증 토큰입니다.");
        }

        logger.info("회사 이메일 인증이 완료되었습니다: {}", token);
    }

    public void requestCompanyUserConversion(Long userId) {
        convertToCompanyUser(userId);
    }

    public void lockAccount(Long userId, LocalDateTime lockUntil) {
        // 현재 스키마에는 계정 잠금 기능이 없으므로 deactivate로 대체
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 비활성화된 계정입니다.");
        }

        user.deactivate();
        userRepository.save(user);

        // 실제 계정 잠금 기능을 위해서는 스키마에 lock_until 필드 추가 또는 별도 테이블 사용 가능
        logger.info("계정 잠금이 완료되었습니다. userId: {}, lockUntil: {}", userId, lockUntil);
    }

    public void unlockAccount(Long userId) {
        // 현재 스키마에는 계정 잠금 기능이 없으므로 activate로 대체
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 활성화된 계정입니다.");
        }

        user.activate();
        userRepository.save(user);

        // 실제 계정 잠금 기능을 위해서는 스키마에 lock_until 필드 추가 또는 별도 테이블 사용 가능
        logger.info("계정 잠금이 해제되었습니다. userId: {}", userId);
    }

    /**
     * 비밀번호 유효성 검증
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 사용자 엔티티 업데이트
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}