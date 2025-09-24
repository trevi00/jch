package org.jbd.backend.job.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.common.dto.PageResponse;
import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.dto.*;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.job.service.JobApplicationService;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> applyForJob(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody JobApplicationCreateDto dto) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.applyToJobPosting(userId, dto.getJobPostingId(), dto.getCoverLetter());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("채용 지원이 완료되었습니다", JobApplicationResponseDto.from(application)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationResponseDto>>> getMyApplications(
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        User user = userService.findUserById(userId);
        Page<JobApplication> applications = jobApplicationService.getJobApplicationsByUser(user, pageable);
        Page<JobApplicationResponseDto> responseDtos = applications.map(JobApplicationResponseDto::from);
        return ResponseEntity.ok(ApiResponse.success("내 지원 내역을 조회했습니다", new PageResponse<>(responseDtos)));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> getApplicationDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.getJobApplication(applicationId);
        
        // 권한 확인 - 지원자 본인이거나 채용공고 작성자인지 확인
        if (!application.getUser().getId().equals(userId) && 
            !application.getJobPosting().getCompanyUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("조회 권한이 없습니다"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("지원 상세 정보를 조회했습니다", JobApplicationResponseDto.from(application)));
    }

    @PutMapping("/{applicationId}/cancel")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> cancelApplication(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.getJobApplication(applicationId);
        
        // 권한 확인 - 지원자 본인인지 확인
        if (!application.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("취소 권한이 없습니다"));
        }
        
        JobApplication rejected = jobApplicationService.rejectJobApplication(applicationId, "지원자가 직접 취소");
        return ResponseEntity.ok(ApiResponse.success("지원이 취소되었습니다", JobApplicationResponseDto.from(rejected)));
    }

    @GetMapping("/job/{jobPostingId}")
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationResponseDto>>> getApplicationsByJobPosting(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobPostingId,
            Pageable pageable) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다"));
        
        // 권한 확인 - 채용공고 작성자인지 확인
        if (!jobPosting.getCompanyUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("조회 권한이 없습니다"));
        }
        
        Page<JobApplication> applications = jobApplicationService.getJobApplicationsByJobPosting(jobPosting, pageable);
        Page<JobApplicationResponseDto> responseDtos = applications.map(JobApplicationResponseDto::from);
        return ResponseEntity.ok(ApiResponse.success("채용공고별 지원자 목록을 조회했습니다", new PageResponse<>(responseDtos)));
    }

    @PutMapping("/{applicationId}/pass-document")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> passDocumentReview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        try {
            Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            JobApplication updated = jobApplicationService.passDocumentReview(applicationId, userId);
            return ResponseEntity.ok(ApiResponse.success("서류 합격 처리되었습니다", JobApplicationResponseDto.from(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{applicationId}/pass-interview")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> passInterview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.getJobApplication(applicationId);
        
        // 권한 확인 - 채용공고 작성자인지 확인
        if (!application.getJobPosting().getCompanyUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("처리 권한이 없습니다"));
        }
        
        JobApplication updated = jobApplicationService.passInterview(applicationId);
        return ResponseEntity.ok(ApiResponse.success("면접 합격 처리되었습니다", JobApplicationResponseDto.from(updated)));
    }

    @PutMapping("/{applicationId}/hire")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> hireApplicant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.getJobApplication(applicationId);
        
        // 권한 확인 - 채용공고 작성자인지 확인
        if (!application.getJobPosting().getCompanyUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("처리 권한이 없습니다"));
        }
        
        JobApplication updated = jobApplicationService.hireApplicant(applicationId);
        return ResponseEntity.ok(ApiResponse.success("최종 합격 처리되었습니다", JobApplicationResponseDto.from(updated)));
    }

    @PutMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> rejectApplication(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason) {
        Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
        JobApplication application = jobApplicationService.getJobApplication(applicationId);
        
        // 권한 확인 - 채용공고 작성자인지 확인
        if (!application.getJobPosting().getCompanyUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("처리 권한이 없습니다"));
        }
        
        JobApplication updated = jobApplicationService.rejectJobApplication(applicationId, reason);
        return ResponseEntity.ok(ApiResponse.success("지원을 거절했습니다", JobApplicationResponseDto.from(updated)));
    }
}