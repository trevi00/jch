package org.jbd.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbd.backend.user.domain.enums.UserType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {

    // 전체 회원 수
    private UserStatisticsDto userStatistics;
    
    // 신규 회원 수 (기간별)
    private NewUserStatisticsDto newUserStatistics;
    
    // 채용공고 통계
    private JobPostingStatisticsDto jobPostingStatistics;
    
    // 지원서 통계
    private ApplicationStatisticsDto applicationStatistics;
    
    // 요청된 증명서 리스트
    private List<CertificateRequestDto> certificateRequests;
    
    // 시스템 통계
    private SystemStatisticsDto systemStatistics;
    
    // AI 서비스 통계
    private AiServiceStatisticsDto aiServiceStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatisticsDto {
        private Long totalUsers;
        private Long generalUsers;
        private Long companyUsers;
        private Long adminUsers;
        private Long activeUsers;
        private Long inactiveUsers;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewUserStatisticsDto {
        private Long todayNewUsers;
        private Long thisWeekNewUsers;
        private Long thisMonthNewUsers;
        private Long todayGeneralUsers;
        private Long todayCompanyUsers;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPostingStatisticsDto {
        private Long totalJobPostings;
        private Long activeJobPostings;
        private Long closedJobPostings;
        private Long thisWeekJobPostings;
        private Double averageApplicationsPerPosting;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationStatisticsDto {
        private Long totalApplications;
        private Long thisWeekApplications;
        private Long pendingApplications;
        private Long successfulApplications;
        private Double successRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateRequestDto {
        private Long requestId;
        private String userEmail;
        private String userName;
        private String certificateType;
        private LocalDateTime requestedAt;
        private String status;
        private String purpose;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemStatisticsDto {
        private Long totalApiCalls;
        private Long dailyActiveUsers;
        private Double systemUptime;
        private Integer errorCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiServiceStatisticsDto {
        // AI 면접 통계
        private Long totalInterviews;
        private Long completedInterviews;
        private Double averageInterviewScore;

        // 이미지 생성 통계
        private Long totalImageGenerations;

        // 감정분석 통계
        private Long totalSentimentAnalyses;
        private Long positivePosts;
        private Long negativePosts;
        private Double positivityRate;
    }
}