package org.jbd.backend.company.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기업 대시보드 종합 정보 DTO
 *
 * 기업이 자신의 채용 현황을 한눈에 파악할 수 있도록
 * 통계, 최근 지원자, 공고 현황 등을 포함한 대시보드 데이터를 제공합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-23
 */
@Getter
@Builder
public class CompanyDashboardDto {

    /**
     * 기업 기본 정보
     */
    private CompanyInfo companyInfo;

    /**
     * 채용 통계 요약
     */
    private HiringStatistics hiringStats;

    /**
     * 최근 지원자 목록 (최근 5명)
     */
    private List<RecentApplicant> recentApplicants;

    /**
     * 내 채용공고 요약 (최근 3개)
     */
    private List<JobPostingSummary> recentJobPostings;

    /**
     * 알림 정보
     */
    private NotificationSummary notifications;

    @Getter
    @Builder
    public static class CompanyInfo {
        private Long companyId;
        private String companyName;
        private String industry;
        private String location;
        private String logoUrl;
        private Boolean isVerified;
    }

    @Getter
    @Builder
    public static class HiringStatistics {
        /**
         * 전체 공고 수
         */
        private Integer totalJobPostings;

        /**
         * 활성 공고 수
         */
        private Integer activeJobPostings;

        /**
         * 전체 지원자 수
         */
        private Integer totalApplicants;

        /**
         * 신규 지원자 수 (오늘)
         */
        private Integer newApplicantsToday;

        /**
         * 신규 지원자 수 (이번 주)
         */
        private Integer newApplicantsThisWeek;

        /**
         * 서류 검토 대기 중인 지원자 수
         */
        private Integer pendingReviewCount;

        /**
         * 면접 예정 지원자 수
         */
        private Integer interviewScheduledCount;

        /**
         * 이번 달 채용 완료 수
         */
        private Integer hiredThisMonth;

        /**
         * 평균 지원율 (조회수 대비 지원 비율)
         */
        private Double averageApplicationRate;
    }

    @Getter
    @Builder
    public static class RecentApplicant {
        private Long applicationId;
        private Long applicantId;
        private String applicantName;
        private String applicantEmail;
        private Long jobPostingId;
        private String jobTitle;
        private String status;
        private LocalDateTime appliedAt;
        private String resumeUrl;
        private String coverLetterPreview; // 자소서 미리보기 (100자)
    }

    @Getter
    @Builder
    public static class JobPostingSummary {
        private Long jobPostingId;
        private String title;
        private String status;
        private Integer viewCount;
        private Integer applicationCount;
        private LocalDateTime createdAt;
        private LocalDateTime deadline;
        private Boolean isDeadlineApproaching; // 마감 7일 이내
        private Integer daysUntilDeadline;
    }

    @Getter
    @Builder
    public static class NotificationSummary {
        /**
         * 신규 지원자 알림 수
         */
        private Integer newApplicationCount;

        /**
         * 마감 임박 공고 수
         */
        private Integer deadlineApproachingCount;

        /**
         * 미처리 업무 수 (서류 검토 등)
         */
        private Integer pendingTaskCount;

        /**
         * 시스템 알림 수
         */
        private Integer systemNotificationCount;
    }
}