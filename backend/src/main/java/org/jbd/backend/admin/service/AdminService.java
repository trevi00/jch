package org.jbd.backend.admin.service;

import lombok.RequiredArgsConstructor;
import org.jbd.backend.admin.dto.*;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.secret-key:ADMIN_SECRET_2024}")
    private String adminSecretKey;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        log.info("Admin login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getUserType() != UserType.ADMIN) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user.getEmail());

        user.updateLastLogin();

        AdminUserInfo userInfo = AdminUserInfo.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .userType(user.getUserType().name())
            .isAdmin(true)
            .role("ADMIN")
            .build();

        return AdminLoginResponse.builder()
            .user(userInfo)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400)
            .build();
    }

    @Transactional
    public void promoteToAdmin(AdminPromoteRequest request) {
        log.info("Admin promotion request for email: {}", request.getEmail());

        try {
            log.debug("Checking secret key...");
            if (!adminSecretKey.equals(request.getSecretKey())) {
                log.error("Invalid secret key provided");
                throw new RuntimeException("잘못된 시크릿 키입니다.");
            }

            log.debug("Finding user by email: {}", request.getEmail());
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.debug("Found user with ID: {} and current type: {}", user.getId(), user.getUserType());
            if (user.getUserType() == UserType.ADMIN) {
                log.warn("User {} is already an admin", request.getEmail());
                throw new RuntimeException("이미 관리자 권한을 가진 사용자입니다.");
            }

            log.debug("Setting user type to ADMIN...");
            user.setUserType(UserType.ADMIN);

            log.debug("Setting admin converted at timestamp...");
            user.setAdminConvertedAt(java.time.LocalDateTime.now());

            log.debug("Saving user to repository...");
            userRepository.save(user);

            log.info("User {} promoted to admin successfully", request.getEmail());
        } catch (Exception e) {
            log.error("Admin promotion failed for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public AdminVerifyResponse verify(String authorization) {
        log.info("Admin verification request");

        String token = authorization.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getUserType() != UserType.ADMIN) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        return AdminVerifyResponse.builder()
            .isAdmin(true)
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role("ADMIN")
            .build();
    }

    public Map<String, Object> getDashboard(String authorization) {
        verify(authorization);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalAdmins", userRepository.countByUserType(UserType.ADMIN));
        dashboard.put("totalGeneralUsers", userRepository.countByUserType(UserType.GENERAL));
        dashboard.put("totalCompanyUsers", userRepository.countByUserType(UserType.COMPANY));

        return dashboard;
    }
}