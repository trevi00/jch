package org.jbd.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;
import org.jbd.backend.dashboard.dto.AdminDashboardDto.UserStatisticsDto;
import org.jbd.backend.dashboard.service.DashboardService;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.service.UserService;
import org.jbd.backend.job.dto.JobPostingResponseDto;
import org.jbd.backend.job.dto.JobPostingCreateDto;
import org.jbd.backend.job.dto.JobPostingUpdateDto;
import org.jbd.backend.job.service.JobPostingService;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.community.dto.PostDto;
import org.jbd.backend.community.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final JwtService jwtService;
    private final DashboardService dashboardService;
    private final JobPostingService jobPostingService;
    private final PostService postService;

    @Value("${app.admin.secret-key:ADMIN_SECRET_2024}")
    private String adminSecretKey;

    /**
     * Test endpoint to verify AdminController is loaded
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("AdminController test endpoint called successfully!");
        return ResponseEntity.ok("AdminController is working properly!");
    }

    /**
     * 일반 사용자를 어드민으로 승급
     */
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(@RequestBody PromoteRequest request) {
        try {
            log.info("Admin promotion request for email: {}", request.getEmail());

            // 시크릿 키 검증
            if (!adminSecretKey.equals(request.getSecretKey())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("잘못된 관리자 시크릿 키입니다."));
            }

            // 사용자 조회
            User user = userService.findUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자를 찾을 수 없습니다."));
            }

            // 이미 어드민인지 확인
            if (user.getUserType() == UserType.ADMIN) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("이미 관리자 권한을 가진 사용자입니다."));
            }

            // 어드민으로 승급
            user.convertToAdmin();
            userService.updateUser(user);

            log.info("User {} promoted to admin successfully", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("관리자 권한이 부여되었습니다."));

        } catch (Exception e) {
            log.error("Admin promotion failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("관리자 권한 부여 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 어드민 로그인 (일반 로그인과 동일하지만 ADMIN 권한 확인)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            log.info("Admin login attempt for email: {}", request.getEmail());

            // 일반 로그인으로 인증
            User user = userService.findUserByEmail(request.getEmail());
            if (user == null || !userService.validatePassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
            }

            // ADMIN 권한 확인
            if (user.getUserType() != UserType.ADMIN) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 필요합니다."));
            }

            // JWT 토큰 생성 (일반 토큰 생성 메서드 사용)
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user.getEmail());

            AdminLoginResponse response = AdminLoginResponse.builder()
                .user(AdminUserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .userType(user.getUserType().name())
                    .isAdmin(true)
                    .role("ADMIN")
                    .build())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400) // 24시간
                .build();

            log.info("Admin login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("관리자 로그인이 완료되었습니다.", response));

        } catch (Exception e) {
            log.error("Admin login failed for email: {}, error: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("로그인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 어드민 권한 검증
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<AdminVerifyResponse>> verifyAdmin(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(jwt);
            User user = userService.findUserByEmail(userEmail);

            if (user == null || user.getUserType() != UserType.ADMIN) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            AdminVerifyResponse response = AdminVerifyResponse.builder()
                .isAdmin(true)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role("ADMIN")
                .build();

            return ResponseEntity.ok(ApiResponse.success("관리자 권한이 확인되었습니다.", response));

        } catch (Exception e) {
            log.error("Admin verification failed, error: {}", e.getMessage());
            return ResponseEntity.status(401)
                .body(ApiResponse.error("토큰 검증에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 어드민 대시보드 데이터 조회
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard(@RequestHeader("Authorization") String token) {
        try {
            // 어드민 권한 검증
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(jwt);
            User user = userService.findUserByEmail(userEmail);

            if (user == null || user.getUserType() != UserType.ADMIN) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 어드민 대시보드 데이터 조회
            AdminDashboardDto dashboardData = dashboardService.getAdminDashboard();

            return ResponseEntity.ok(ApiResponse.success("관리자 대시보드 조회 성공", dashboardData));

        } catch (Exception e) {
            log.error("Admin dashboard fetch failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("대시보드 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 사용자 조회 (관리자용)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<java.util.List<UserResponseDto>>> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            // 어드민 권한 검증
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(jwt);
            User user = userService.findUserByEmail(userEmail);

            if (user == null || user.getUserType() != UserType.ADMIN) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 모든 사용자 조회
            java.util.List<UserResponseDto> users = userService.getAllUsers();

            return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", users));

        } catch (Exception e) {
            log.error("Admin get all users failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 통계 조회 (관리자용)
     */
    @GetMapping("/users/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsDto>> getUserStatistics(@RequestHeader("Authorization") String token) {
        try {
            // 어드민 권한 검증
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(jwt);
            User user = userService.findUserByEmail(userEmail);

            if (user == null || user.getUserType() != UserType.ADMIN) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 사용자 통계 조회
            AdminDashboardDto dashboardData = dashboardService.getAdminDashboard();
            UserStatisticsDto userStats = dashboardData.getUserStatistics();

            return ResponseEntity.ok(ApiResponse.success("사용자 통계 조회 성공", userStats));

        } catch (Exception e) {
            log.error("Admin get user statistics failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("사용자 통계 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // =========================== 채용공고 관리 (Admin) ===========================

    /**
     * 모든 채용공고 조회 (관리자용)
     */
    @GetMapping("/job-postings")
    public ResponseEntity<ApiResponse<java.util.List<JobPostingResponseDto>>> getAllJobPostings(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 어드민 권한 검증
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 모든 채용공고 조회
            var jobPostings = jobPostingService.getPublishedJobPostings(
                org.springframework.data.domain.PageRequest.of(page, size));

            return ResponseEntity.ok(ApiResponse.success("모든 채용공고 조회 성공", jobPostings.getContent().stream()
                .map(JobPostingResponseDto::from)
                .collect(java.util.stream.Collectors.toList())));

        } catch (Exception e) {
            log.error("Admin get all job postings failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("채용공고 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 채용공고 생성 (관리자용)
     */
    @PostMapping("/job-postings")
    public ResponseEntity<ApiResponse<JobPostingResponseDto>> createJobPosting(
            @RequestHeader("Authorization") String token,
            @RequestBody JobPostingCreateDto request) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 관리자용 채용공고 생성 - 임시로 간단한 구조 사용
            User adminUser = userService.findUserByEmail(userEmail);
            JobPosting jobPosting = jobPostingService.createJobPosting(
                adminUser.getId(),
                request.getTitle(),
                request.getCompanyName(),
                request.getLocation(),
                request.getJobType(),
                request.getExperienceLevel(),
                request.getDescription(),
                request.getMinSalary(),
                request.getMaxSalary()
            );

            JobPostingResponseDto response = JobPostingResponseDto.from(jobPosting);

            return ResponseEntity.ok(ApiResponse.success("채용공고 생성 성공", response));

        } catch (Exception e) {
            log.error("Admin create job posting failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("채용공고 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 채용공고 수정 (관리자용)
     */
    @PutMapping("/job-postings/{jobId}")
    public ResponseEntity<ApiResponse<JobPostingResponseDto>> updateJobPosting(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @RequestBody JobPostingUpdateDto request) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 채용공고 수정 (관리자는 모든 채용공고 수정 가능)
            JobPosting jobPosting = jobPostingService.updateJobPosting(jobId, request);
            JobPostingResponseDto response = JobPostingResponseDto.from(jobPosting);

            return ResponseEntity.ok(ApiResponse.success("채용공고 수정 성공", response));

        } catch (Exception e) {
            log.error("Admin update job posting failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("채용공고 수정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 채용공고 삭제 (관리자용)
     */
    @DeleteMapping("/job-postings/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 채용공고 삭제 (관리자는 모든 채용공고 삭제 가능)
            jobPostingService.deleteJobPosting(jobId);

            return ResponseEntity.ok(ApiResponse.success("채용공고 삭제 성공"));

        } catch (Exception e) {
            log.error("Admin delete job posting failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("채용공고 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // =========================== 커뮤니티 관리 (Admin) ===========================

    /**
     * 모든 게시글 조회 (관리자용)
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PostDto.PageResponse>> getAllPosts(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 어드민 권한 검증
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 모든 게시글 조회
            PostDto.PageResponse posts = postService.getAllPosts(
                org.springframework.data.domain.PageRequest.of(page, size));

            return ResponseEntity.ok(ApiResponse.success("모든 게시글 조회 성공", posts));

        } catch (Exception e) {
            log.error("Admin get all posts failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("게시글 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 공지사항 게시글 생성 (관리자 전용)
     */
    @PostMapping("/posts/notice")
    public ResponseEntity<ApiResponse<PostDto.Response>> createNoticePost(
            @RequestHeader("Authorization") String token,
            @RequestBody PostDto.CreateRequest request) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 공지사항 게시글 생성 (카테고리를 공지로 설정)
            PostDto.Response post = postService.createPost(request, userEmail);

            return ResponseEntity.ok(ApiResponse.success("공지사항 생성 성공", post));

        } catch (Exception e) {
            log.error("Admin create notice post failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("공지사항 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 게시글 수정 (관리자용)
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDto.Response>> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @RequestBody PostDto.UpdateRequest request) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 게시글 수정 (관리자는 모든 게시글 수정 가능)
            PostDto.Response post = postService.updatePost(postId, request, userEmail);

            return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", post));

        } catch (Exception e) {
            log.error("Admin update post failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("게시글 수정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 게시글 삭제 (관리자용)
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        try {
            // 어드민 권한 검증
            String userEmail = extractUserEmailFromToken(token);
            if (!verifyAdminToken(token)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("관리자 권한이 없습니다."));
            }

            // 게시글 삭제 (관리자는 모든 게시글 삭제 가능)
            postService.deletePost(postId, userEmail);

            return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공"));

        } catch (Exception e) {
            log.error("Admin delete post failed, error: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // =========================== 공통 헬퍼 메서드 ===========================

    /**
     * 관리자 토큰 검증
     */
    private boolean verifyAdminToken(String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(jwt);
            User user = userService.findUserByEmail(userEmail);

            return user != null && user.getUserType() == UserType.ADMIN;
        } catch (Exception e) {
            log.error("Admin token verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    private String extractUserEmailFromToken(String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            return jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.error("Token email extraction failed: {}", e.getMessage());
            return null;
        }
    }

    // DTO Classes
    public static class PromoteRequest {
        private String email;
        private String secretKey;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class AdminLoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AdminLoginResponse {
        private AdminUserInfo user;
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;

        public static AdminLoginResponse.Builder builder() {
            return new AdminLoginResponse.Builder();
        }

        public static class Builder {
            private AdminUserInfo user;
            private String accessToken;
            private String refreshToken;
            private String tokenType;
            private long expiresIn;

            public Builder user(AdminUserInfo user) { this.user = user; return this; }
            public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
            public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
            public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
            public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }

            public AdminLoginResponse build() {
                AdminLoginResponse response = new AdminLoginResponse();
                response.user = this.user;
                response.accessToken = this.accessToken;
                response.refreshToken = this.refreshToken;
                response.tokenType = this.tokenType;
                response.expiresIn = this.expiresIn;
                return response;
            }
        }

        // Getters
        public AdminUserInfo getUser() { return user; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType() { return tokenType; }
        public long getExpiresIn() { return expiresIn; }
    }

    public static class AdminUserInfo {
        private Long id;
        private String email;
        private String name;
        private String userType;
        private boolean isAdmin;
        private String role;

        public static AdminUserInfo.Builder builder() {
            return new AdminUserInfo.Builder();
        }

        public static class Builder {
            private Long id;
            private String email;
            private String name;
            private String userType;
            private boolean isAdmin;
            private String role;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder userType(String userType) { this.userType = userType; return this; }
            public Builder isAdmin(boolean isAdmin) { this.isAdmin = isAdmin; return this; }
            public Builder role(String role) { this.role = role; return this; }

            public AdminUserInfo build() {
                AdminUserInfo userInfo = new AdminUserInfo();
                userInfo.id = this.id;
                userInfo.email = this.email;
                userInfo.name = this.name;
                userInfo.userType = this.userType;
                userInfo.isAdmin = this.isAdmin;
                userInfo.role = this.role;
                return userInfo;
            }
        }

        // Getters
        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getUserType() { return userType; }
        public boolean getIsAdmin() { return isAdmin; }
        public String getRole() { return role; }
    }

    public static class AdminVerifyResponse {
        private boolean isAdmin;
        private Long userId;
        private String email;
        private String name;
        private String role;

        public static AdminVerifyResponse.Builder builder() {
            return new AdminVerifyResponse.Builder();
        }

        public static class Builder {
            private boolean isAdmin;
            private Long userId;
            private String email;
            private String name;
            private String role;

            public Builder isAdmin(boolean isAdmin) { this.isAdmin = isAdmin; return this; }
            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder role(String role) { this.role = role; return this; }

            public AdminVerifyResponse build() {
                AdminVerifyResponse response = new AdminVerifyResponse();
                response.isAdmin = this.isAdmin;
                response.userId = this.userId;
                response.email = this.email;
                response.name = this.name;
                response.role = this.role;
                return response;
            }
        }

        // Getters
        public boolean getIsAdmin() { return isAdmin; }
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }
}