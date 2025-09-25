package org.jbd.backend.user.controller;

import jakarta.validation.Valid;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.dto.UserUpdateDto;
import org.jbd.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 관리 REST API 컨트롤러
 *
 * 사용자 정보 조회, 수정, 삭제, 이메일 인증, 계정 잠금/해제 등 사용자와 관련된 API를 제공합니다.
 * 일반 사용자용 API와 관리자용 API를 포함하며, 적절한 권한 검증을 통해 보안을 보장합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see UserService
 * @see UserResponseDto
 * @see UserUpdateDto
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    /** 사용자 비즈니스 로직을 처리하는 서비스 */
    private final UserService userService;

    /** JWT 토큰 관련 작업을 처리하는 서비스 */
    private final JwtService jwtService;

    /**
     * UserController 생성자
     *
     * @param userService 사용자 서비스
     * @param jwtService JWT 토큰 서비스
     */
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    /**
     * 현재 인증된 사용자의 정보를 조회합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<UserResponseDto>> 사용자 정보
     * @apiNote GET /api/users/me
     * @see UserResponseDto
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    /**
     * 현재 인증된 사용자의 정보를 수정합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @param updateDto 수정할 사용자 정보
     *                  - name: 사용자 이름 (선택)
     *                  - phone: 전화번호 (선택)
     *                  - address: 주소 (선택)
     * @return ResponseEntity<ApiResponse<UserResponseDto>> 수정된 사용자 정보
     * @apiNote PUT /api/users/me
     * @see UserUpdateDto
     * @see UserResponseDto
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateDto updateDto
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        UserResponseDto updatedUser = userService.updateUser(userId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 수정되었습니다.", updatedUser));
    }
    
    /**
     * 현재 인증된 사용자의 계정을 삭제합니다.
     * 삭제된 계정은 복구할 수 없습니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<Void>> 삭제 완료 응답
     * @apiNote DELETE /api/users/me
     */
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정이 삭제되었습니다."));
    }
    
    /**
     * 이메일 인증을 처리합니다.
     * 회원가입 시 발송된 인증 링크를 통해 이메일 주소를 확인합니다.
     *
     * @param token 이메일 인증 토큰
     * @return ResponseEntity<ApiResponse<Void>> 인증 완료 응답
     * @apiNote GET /api/users/verify-email/{token}
     */
    @GetMapping("/verify-email/{token}")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다."));
    }
    
    /**
     * 회사 이메일 인증을 요청합니다.
     * 기업 사용자 전환을 위해 회사 이메일 주소 확인이 필요합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @param companyEmail 인증할 회사 이메일 주소
     * @return ResponseEntity<ApiResponse<Void>> 인증 요청 완료 응답
     * @apiNote POST /api/users/request-company-email-verification
     */
    @PostMapping("/request-company-email-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> requestCompanyEmailVerification(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String companyEmail
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        userService.requestCompanyEmailVerification(userId, companyEmail);
        return ResponseEntity.ok(ApiResponse.success("회사 이메일 인증 요청이 전송되었습니다."));
    }
    
    /**
     * 회사 이메일 인증을 처리합니다.
     * 회사 이메일로 발송된 인증 링크를 통해 회사 이메일 주소를 확인합니다.
     *
     * @param token 회사 이메일 인증 토큰
     * @return ResponseEntity<ApiResponse<Void>> 인증 완료 응답
     * @apiNote GET /api/users/verify-company-email/{token}
     */
    @GetMapping("/verify-company-email/{token}")
    public ResponseEntity<ApiResponse<Void>> verifyCompanyEmail(@PathVariable String token) {
        userService.verifyCompanyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("회사 이메일 인증이 완료되었습니다."));
    }
    
    /**
     * 기업 사용자 전환을 요청합니다.
     * 일반 사용자가 기업 사용자로 계정 유형을 변경할 때 사용합니다.
     * 회사 이메일 인증이 선행되어야 합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<Void>> 전환 완료 응답
     * @apiNote POST /api/users/request-company-user-conversion
     */
    @PostMapping("/request-company-user-conversion")
    @PreAuthorize("isAuthenticated() and hasRole('GENERAL')")
    public ResponseEntity<ApiResponse<Void>> requestCompanyUserConversion(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        userService.requestCompanyUserConversion(userId);
        return ResponseEntity.ok(ApiResponse.success("기업 유저로 전환되었습니다."));
    }
    
    /**
     * 사용자의 비밀번호를 변경합니다.
     * 기존 비밀번호 확인 후 새로운 비밀번호로 변경합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새로운 비밀번호
     * @return ResponseEntity<ApiResponse<Void>> 변경 완료 응답
     * @apiNote POST /api/users/change-password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String currentPassword,
            @RequestParam String newPassword
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
    }
    
    /**
     * 모든 사용자 목록을 조회합니다. (관리자 전용)
     * 사용자 유형을 지정하여 필터링할 수 있습니다.
     *
     * @param userType 사용자 유형 (선택사항: GENERAL, COMPANY, ADMIN)
     * @return ResponseEntity<ApiResponse<List<UserResponseDto>>> 사용자 목록
     * @apiNote GET /api/users
     * @see UserType
     * @see UserResponseDto
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(
            @RequestParam(required = false) UserType userType
    ) {
        List<UserResponseDto> users = userType != null
                ? userService.getUsersByType(userType)
                : userService.getAllUsers(); // 모든 사용자 반환

        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    /**
     * 특정 사용자의 정보를 조회합니다. (관리자 전용)
     *
     * @param userId 사용자 ID
     * @return ResponseEntity<ApiResponse<UserResponseDto>> 사용자 정보
     * @apiNote GET /api/users/{userId}
     * @see UserResponseDto
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long userId) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    
    /**
     * 사용자 계정을 잠급니다. (관리자 전용)
     * 지정된 기간까지 또는 기본 7일간 로그인을 차단합니다.
     *
     * @param userId 잠금할 사용자 ID
     * @param until 잠금 해제 일시 (ISO LocalDateTime 형식, 선택사항)
     * @return ResponseEntity<ApiResponse<Void>> 잠금 완료 응답
     * @apiNote POST /api/users/{userId}/lock-account
     */
    @PostMapping("/{userId}/lock-account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockAccount(
            @PathVariable Long userId,
            @RequestParam(required = false) String until
    ) {
        LocalDateTime lockUntil = until != null 
                ? LocalDateTime.parse(until)
                : LocalDateTime.now().plusDays(7); // 기본 7일
        
        userService.lockAccount(userId, lockUntil);
        return ResponseEntity.ok(ApiResponse.success("계정이 잠겼습니다."));
    }
    
    /**
     * 사용자 계정 잠금을 해제합니다. (관리자 전용)
     *
     * @param userId 잠금을 해제할 사용자 ID
     * @return ResponseEntity<ApiResponse<Void>> 잠금 해제 완료 응답
     * @apiNote POST /api/users/{userId}/unlock-account
     */
    @PostMapping("/{userId}/unlock-account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long userId) {
        userService.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("계정 잠금이 해제되었습니다."));
    }
    
    /**
     * 사용자 통계 정보를 조회합니다. (관리자 전용)
     * 사용자 유형별 수, 활성 사용자 수, 잠김 사용자 수 등을 제공합니다.
     *
     * @return ResponseEntity<ApiResponse<UserStatistics>> 사용자 통계 정보
     * @apiNote GET /api/users/statistics
     * @see UserStatistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatistics>> getUserStatistics() {
        long generalCount = userService.countUsersByType(UserType.GENERAL);
        long companyCount = userService.countUsersByType(UserType.COMPANY);
        long adminCount = userService.countUsersByType(UserType.ADMIN);
        long totalUsers = generalCount + companyCount + adminCount - 1;

        // 임시로 활성 사용자와 잠금 사용자 수 계산 (실제로는 userService에서 구현)
        long activeUsers = totalUsers; // 임시로 모든 사용자가 활성 상태라고 가정
        long lockedUsers = 0; // 임시로 잠금된 사용자 없음

        UserStatistics statistics = new UserStatistics(
                totalUsers, generalCount, companyCount, adminCount, activeUsers, lockedUsers
        );

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    /**
     * 사용자 통계 정보를 담는 DTO 클래스
     *
     * 시스템 내 사용자 현황을 유형별, 상태별로 집계한 정보를 제공합니다.
     * 관리자 대시보드에서 사용자 현황을 파악하는 데 사용됩니다.
     *
     * @author JBD Backend Team
     * @version 1.0
     * @since 2025-09-19
     */
    public static class UserStatistics {
        /** 전체 사용자 수 */
        private final long totalUsers;
        /** 일반 사용자 수 */
        private final long generalUsers;
        /** 기업 사용자 수 */
        private final long companyUsers;
        /** 관리자 수 */
        private final long adminUsers;
        /** 활성 사용자 수 */
        private final long activeUsers;
        /** 잠금된 사용자 수 */
        private final long lockedUsers;

        /**
         * UserStatistics 생성자
         *
         * @param totalUsers 전체 사용자 수
         * @param generalUsers 일반 사용자 수
         * @param companyUsers 기업 사용자 수
         * @param adminUsers 관리자 수
         * @param activeUsers 활성 사용자 수
         * @param lockedUsers 잠금된 사용자 수
         */
        public UserStatistics(long totalUsers, long generalUsers, long companyUsers,
                            long adminUsers, long activeUsers, long lockedUsers) {
            this.totalUsers = totalUsers;
            this.generalUsers = generalUsers;
            this.companyUsers = companyUsers;
            this.adminUsers = adminUsers;
            this.activeUsers = activeUsers;
            this.lockedUsers = lockedUsers;
        }

        /** @return 전체 사용자 수 */
        public long getTotalUsers() { return totalUsers; }

        /** @return 일반 사용자 수 */
        public long getGeneralUsers() { return generalUsers; }

        /** @return 기업 사용자 수 */
        public long getCompanyUsers() { return companyUsers; }

        /** @return 관리자 수 */
        public long getAdminUsers() { return adminUsers; }

        /** @return 활성 사용자 수 */
        public long getActiveUsers() { return activeUsers; }

        /** @return 잠금된 사용자 수 */
        public long getLockedUsers() { return lockedUsers; }
    }
}