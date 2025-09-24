package org.jbd.backend.company.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jbd.backend.job.domain.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기업의 지원자 관리를 위한 DTO 클래스들
 *
 * 기업이 자신의 채용공고에 지원한 지원자들을 효율적으로 관리할 수 있도록
 * 지원자 목록 조회, 상세 정보, 상태 변경 등의 기능을 지원합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-23
 */
public class ApplicantManagementDto {

    /**
     * 지원자 목록 조회 응답 DTO
     */
    @Getter
    @Builder
    public static class ApplicantListResponse {
        private List<ApplicantSummary> applicants;
        private Integer totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
        private ApplicantStatistics statistics;
    }

    /**
     * 지원자 요약 정보 DTO
     */
    @Getter
    @Builder
    public static class ApplicantSummary {
        private Long applicationId;
        private Long applicantId;
        private String applicantName;
        private String applicantEmail;
        private String applicantPhone;
        private Long jobPostingId;
        private String jobTitle;
        private ApplicationStatus status;
        private LocalDateTime appliedAt;
        private LocalDateTime lastUpdatedAt;
        private String coverLetterPreview; // 자소서 미리보기 (150자)
        private Integer experience; // 경력 (년)
        private String profileImageUrl;
        private Double matchScore; // AI 매칭 점수 (0-100)
        private List<String> skills; // 주요 스킬
        private String education; // 최종 학력
    }

    /**
     * 지원자 상세 정보 DTO
     */
    @Getter
    @Builder
    public static class ApplicantDetail {
        private Long applicationId;
        private ApplicantProfile applicantProfile;
        private ApplicationInfo applicationInfo;
        private List<InteractionHistory> interactionHistory;
        private AIAnalysis aiAnalysis;
    }

    /**
     * 지원자 프로필 정보
     */
    @Getter
    @Builder
    public static class ApplicantProfile {
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String profileImageUrl;
        private String resume; // 이력서 전문
        private List<Experience> experiences;
        private List<Education> educations;
        private List<Skill> skills;
        private String introduction; // 자기소개
    }

    /**
     * 지원 정보
     */
    @Getter
    @Builder
    public static class ApplicationInfo {
        private Long jobPostingId;
        private String jobTitle;
        private ApplicationStatus status;
        private LocalDateTime appliedAt;
        private String coverLetter; // 자기소개서 전문
        private String resumeFileUrl; // 첨부된 이력서 파일
        private String interviewerNotes; // 면접관 메모
        private String rejectionReason; // 불합격 사유
        private LocalDateTime interviewScheduledAt; // 면접 예정일
    }

    /**
     * 경력 정보
     */
    @Getter
    @Builder
    public static class Experience {
        private String companyName;
        private String position;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isCurrentJob;
        private String description;
    }

    /**
     * 학력 정보
     */
    @Getter
    @Builder
    public static class Education {
        private String schoolName;
        private String major;
        private String degree;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String gpa;
    }

    /**
     * 스킬 정보
     */
    @Getter
    @Builder
    public static class Skill {
        private String skillName;
        private String level; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
        private Integer experienceYears;
    }

    /**
     * 상호작용 이력
     */
    @Getter
    @Builder
    public static class InteractionHistory {
        private LocalDateTime timestamp;
        private String action; // VIEW, STATUS_CHANGE, NOTE_ADD, INTERVIEW_SCHEDULE
        private String details;
        private String performedBy; // 수행자 이름
    }

    /**
     * AI 분석 정보
     */
    @Getter
    @Builder
    public static class AIAnalysis {
        private Double matchScore; // 매칭 점수 (0-100)
        private List<String> strengths; // 강점
        private List<String> concerns; // 우려사항
        private String recommendation; // AI 추천 의견
        private List<String> keywordMatches; // 키워드 매칭
    }

    /**
     * 지원자 통계 정보
     */
    @Getter
    @Builder
    public static class ApplicantStatistics {
        private Integer totalApplicants;
        private Integer newApplicants; // 신규 (PENDING)
        private Integer underReview; // 검토중 (REVIEWING)
        private Integer interviewScheduled; // 면접예정 (INTERVIEW)
        private Integer hired; // 채용완료 (ACCEPTED)
        private Integer rejected; // 불합격 (REJECTED)
    }

    /**
     * 지원자 상태 변경 요청 DTO
     */
    @Getter
    @Setter
    public static class StatusUpdateRequest {
        private ApplicationStatus newStatus;
        private String notes; // 상태 변경 사유/메모
        private LocalDateTime interviewScheduledAt; // 면접 예정일 (상태가 INTERVIEW인 경우)
        private String rejectionReason; // 불합격 사유 (상태가 REJECTED인 경우)
    }

    /**
     * 일괄 작업 요청 DTO
     */
    @Getter
    @Setter
    public static class BulkActionRequest {
        private List<Long> applicationIds;
        private String action; // BULK_STATUS_UPDATE, BULK_DELETE, BULK_EXPORT
        private ApplicationStatus newStatus; // 일괄 상태 변경용
        private String notes; // 일괄 작업 메모
    }

    /**
     * 지원자 검색/필터링 요청 DTO
     */
    @Getter
    @Setter
    public static class ApplicantSearchRequest {
        private String keyword; // 이름, 이메일 검색
        private ApplicationStatus status; // 상태별 필터
        private Long jobPostingId; // 특정 공고별 필터
        private Integer minExperience; // 최소 경력
        private Integer maxExperience; // 최대 경력
        private List<String> skills; // 스킬별 필터
        private String education; // 학력별 필터
        private LocalDateTime appliedAfter; // 지원일 이후
        private LocalDateTime appliedBefore; // 지원일 이전
        private Double minMatchScore; // 최소 매칭 점수
        private String sortBy; // 정렬 기준 (appliedAt, matchScore, name)
        private String sortDirection; // 정렬 방향 (ASC, DESC)
    }
}