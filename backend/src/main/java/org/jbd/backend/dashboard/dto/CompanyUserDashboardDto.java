package org.jbd.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbd.backend.job.dto.JobPostingResponseDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserDashboardDto {

    // 등록한 채용공고 수
    private Integer totalJobPostings;
    
    // 활성 채용공고 수
    private Integer activeJobPostings;
    
    // 전체 지원자 수
    private Integer totalApplications;
    
    // 신규 지원자 수 (최근 7일)
    private Integer newApplicationsThisWeek;
    
    // 내가 올린 채용공고 리스트
    private List<JobPostingResponseDto> myJobPostings;
    
    // 인기 채용공고 리스트
    private List<PopularJobPostingDto> popularJobPostings;
    
    // 지원자 통계
    private ApplicationStatisticsDto applicationStatistics;
    
    // 빠른 실행 메뉴
    private CompanyQuickActionsDto quickActions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularJobPostingDto {
        private Long jobPostingId;
        private String title;
        private String companyName;
        private Integer applicationCount;
        private Integer viewCount;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationStatisticsDto {
        private Integer pendingReview;
        private Integer documentPassed;
        private Integer interviewScheduled;
        private Integer finalPassed;
        private Integer rejected;
        private Double averageApplicationsPerPosting;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyQuickActionsDto {
        private String createJobPostingUrl;
        private String manageApplicationsUrl;
        private String companyProfileUrl;
        private String statisticsUrl;
    }
}