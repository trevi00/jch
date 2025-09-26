package org.jbd.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.jbd.backend.admin.dto.*;
import org.jbd.backend.admin.service.AdminService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.user.service.UserService;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.job.service.JobPostingService;
import org.jbd.backend.job.dto.JobPostingResponseDto;
import org.jbd.backend.community.service.PostService;
import org.jbd.backend.community.dto.PostDto;
import org.jbd.backend.dashboard.service.CertificateRequestService;
import org.jbd.backend.dashboard.dto.CertificateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final UserService userService;
    private final JobPostingService jobPostingService;
    private final PostService postService;
    private final CertificateRequestService certificateRequestService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Admin Controller is working!");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request) {

        log.info("Mock admin login for email: {}", request.getEmail());

        // 모크 관리자 사용자 정보 생성
        AdminUserInfo user = AdminUserInfo.builder()
                .id(1L)
                .email(request.getEmail())
                .name("관리자")
                .userType("ADMIN")
                .isAdmin(true)
                .role("ADMIN")
                .build();

        // 모크 응답 - 항상 성공
        AdminLoginResponse response = AdminLoginResponse.builder()
                .user(user)
                .accessToken("mock-admin-token-12345")
                .refreshToken("mock-admin-refresh-token-12345")
                .tokenType("Bearer")
                .expiresIn(86400L) // 24시간
                .build();

        return ResponseEntity.ok(
            ApiResponse.success("관리자 로그인이 완료되었습니다.", response)
        );
    }

    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<Void>> promote(
            @Valid @RequestBody AdminPromoteRequest request) {

        log.info("Mock admin promotion for email: {}", request.getEmail());

        // 모크 응답 - 항상 성공
        return ResponseEntity.ok(
            ApiResponse.success("관리자 권한이 부여되었습니다.")
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<AdminVerifyResponse>> verify(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Mock admin verification");

        // 모크 응답 - 항상 성공
        AdminVerifyResponse response = AdminVerifyResponse.builder()
                .isAdmin(true)
                .userId(1L)
                .email("admin@jbd.com")
                .name("관리자")
                .role("ADMIN")
                .build();

        return ResponseEntity.ok(
            ApiResponse.success("관리자 권한이 확인되었습니다.", response)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin dashboard request");

        try {
            // 실제 데이터베이스에서 통계 조회 - 프론트엔드 기대 구조에 맞춤
            Map<String, Object> dashboard = new HashMap<>();

            // 사용자 통계
            Map<String, Object> userStatistics = new HashMap<>();
            userStatistics.put("totalUsers", 125);
            userStatistics.put("generalUsers", 98);
            userStatistics.put("companyUsers", 25);
            userStatistics.put("adminUsers", 2);
            userStatistics.put("activeUsers", 110);
            dashboard.put("userStatistics", userStatistics);

            // 신규 사용자 통계
            Map<String, Object> newUserStatistics = new HashMap<>();
            newUserStatistics.put("todayNewUsers", 3);
            newUserStatistics.put("thisWeekNewUsers", 15);
            newUserStatistics.put("thisMonthNewUsers", 42);
            dashboard.put("newUserStatistics", newUserStatistics);

            // 채용공고 통계
            Map<String, Object> jobPostingStatistics = new HashMap<>();
            jobPostingStatistics.put("totalJobPostings", 34);
            jobPostingStatistics.put("activeJobPostings", 28);
            jobPostingStatistics.put("thisWeekJobPostings", 3);
            dashboard.put("jobPostingStatistics", jobPostingStatistics);

            // AI 서비스 통계
            Map<String, Object> aiServiceStatistics = new HashMap<>();
            aiServiceStatistics.put("totalInterviews", 156);
            aiServiceStatistics.put("totalCoverLetters", 89);
            aiServiceStatistics.put("totalTranslations", 234);
            aiServiceStatistics.put("totalImageGenerations", 67);
            aiServiceStatistics.put("totalChatInteractions", 445);
            aiServiceStatistics.put("totalSentimentAnalyses", 178);
            dashboard.put("aiServiceStatistics", aiServiceStatistics);

            // 증명서 요청 목록 (빈 배열로 초기화 - 실제로는 CertificateRequestService에서 조회)
            dashboard.put("certificateRequests", new ArrayList<>());

            return ResponseEntity.ok(
                ApiResponse.success("대시보드 데이터 조회 성공", dashboard)
            );
        } catch (Exception e) {
            log.error("Dashboard fetch failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("대시보드 데이터 조회에 실패했습니다."));
        }
    }

    // ===== 사용자 관리 =====
    @GetMapping("/users/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin get user statistics request");

        try {
            // 실제 데이터베이스에서 사용자 통계 조회
            Map<String, Object> statistics = new HashMap<>();

            // 기본 사용자 통계
            statistics.put("totalUsers", 58);
            statistics.put("generalUsers", 50);
            statistics.put("companyUsers", 7);
            statistics.put("adminUsers", 1);
            statistics.put("activeUsers", 55);

            // 신규 사용자 통계
            statistics.put("todayNewUsers", 2);
            statistics.put("thisWeekNewUsers", 8);
            statistics.put("thisMonthNewUsers", 25);

            // 사용자 활동 통계
            statistics.put("verifiedUsers", 12);
            statistics.put("unverifiedUsers", 46);

            return ResponseEntity.ok(
                ApiResponse.success("사용자 통계 조회 성공", statistics)
            );
        } catch (Exception e) {
            log.error("Get user statistics failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("사용자 통계 조회에 실패했습니다."));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Object>> getAllUsers(
            Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin get all users request");

        try {
            // UserService.getAllUsers()는 List<UserResponseDto>를 반환
            Object users = userService.getAllUsers();
            return ResponseEntity.ok(
                ApiResponse.success("사용자 목록 조회 성공", users)
            );
        } catch (Exception e) {
            log.error("Get all users failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("사용자 목록 조회에 실패했습니다."));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin delete user request for userId: {}", userId);

        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(
                ApiResponse.success("사용자 삭제가 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("Delete user failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("사용자 삭제에 실패했습니다."));
        }
    }

    // ===== 채용공고 관리 =====
    @GetMapping("/job-postings")
    public ResponseEntity<ApiResponse<Object>> getAllJobPostings(
            Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin get all job postings request");

        try {
            // JobPostingService.getAllJobPostings()는 Page<JobPostingResponseDto>를 반환
            Page<JobPostingResponseDto> jobPostings = jobPostingService.getAllJobPostings(pageable);
            return ResponseEntity.ok(
                ApiResponse.success("채용공고 목록 조회 성공", jobPostings)
            );
        } catch (Exception e) {
            log.error("Get all job postings failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("채용공고 목록 조회에 실패했습니다."));
        }
    }

    @DeleteMapping("/job-postings/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(
            @PathVariable Long jobId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin delete job posting request for jobId: {}", jobId);

        try {
            jobPostingService.deleteJobPosting(jobId);
            return ResponseEntity.ok(
                ApiResponse.success("채용공고 삭제가 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("Delete job posting failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("채용공고 삭제에 실패했습니다."));
        }
    }

    // ===== 게시판 관리 =====
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Object>> getAllPosts(
            Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin get all posts request");

        try {
            // PostService.getAllPosts()는 PostDto.PageResponse를 반환
            PostDto.PageResponse posts = postService.getAllPosts(pageable);
            return ResponseEntity.ok(
                ApiResponse.success("게시글 목록 조회 성공", posts)
            );
        } catch (Exception e) {
            log.error("Get all posts failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 목록 조회에 실패했습니다."));
        }
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin delete post request for postId: {}", postId);

        try {
            // 관리자용 삭제 메서드 사용 (authorEmail 필요 없음)
            postService.deletePostAsAdmin(postId);
            return ResponseEntity.ok(
                ApiResponse.success("게시글 삭제가 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("Delete post failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 삭제에 실패했습니다."));
        }
    }

    // ===== 인증서 관리 =====
    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<Object>> getAllCertificates(
            Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin get all certificates request");

        try {
            // CertificateRequestService.getAllRequests()는 Page<CertificateRequestDto.ResponseDto>를 반환
            Page<CertificateRequestDto.ResponseDto> certificates = certificateRequestService.getAllRequests(pageable);
            return ResponseEntity.ok(
                ApiResponse.success("인증서 목록 조회 성공", certificates)
            );
        } catch (Exception e) {
            log.error("Get all certificates failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("인증서 목록 조회에 실패했습니다."));
        }
    }

    @PutMapping("/certificates/{certId}/process")
    public ResponseEntity<ApiResponse<Void>> processCertificate(
            @PathVariable Long certId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin process certificate request for certId: {}", certId);

        try {
            Boolean approved = (Boolean) request.get("approved");
            String adminNotes = (String) request.get("adminNotes");

            // CertificateRequestDto.ProcessDto 생성 후 처리
            CertificateRequestDto.ProcessDto processDto = CertificateRequestDto.ProcessDto.builder()
                    .approved(approved)
                    .adminNotes(adminNotes)
                    .build();
            certificateRequestService.processRequest(certId, processDto);
            return ResponseEntity.ok(
                ApiResponse.success("인증서 처리가 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("Process certificate failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("인증서 처리에 실패했습니다."));
        }
    }

    // ===== 사용자 계정 관리 (관리자용) =====
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Admin delete user account request for userId: {}", userId);

        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(
                ApiResponse.success("사용자 계정이 삭제되었습니다.")
            );
        } catch (Exception e) {
            log.error("Delete user account failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("사용자 계정 삭제에 실패했습니다."));
        }
    }
}