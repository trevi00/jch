package org.jbd.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralUserDashboardDto {

    // 전체 취업률
    private Double totalEmploymentRate;
    
    // 나의 취업 점수
    private Integer myJobScore;
    
    // 나의 지원 현황
    private MyApplicationStatusDto myApplicationStatus;
    
    // 직무별 취업 현황
    private List<JobFieldEmploymentDto> jobFieldEmployments;
    
    // 개인 분석 인사이트
    private PersonalInsightDto personalInsight;
    
    // 빠른 실행 메뉴
    private QuickActionsDto quickActions;
    
    // 취업 준비도 분석 데이터
    private JobPreparationAnalysisDto jobPreparationAnalysis;
    
    // 월별 진행 상황 데이터
    private List<MonthlyProgressDto> monthlyProgress;
    
    // 역량 분석 데이터
    private List<CapabilityDto> capabilities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyApplicationStatusDto {
        private Integer totalApplications;
        private Integer pendingApplications;
        private Integer interviewApplications;
        private Integer rejectedApplications;
        private Integer acceptedApplications;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobFieldEmploymentDto {
        private String jobField;
        private Double employmentRate;
        private Integer totalApplicants;
        private Integer employedCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInsightDto {
        private List<String> recommendations;
        private List<String> skillsToImprove;
        private List<String> suggestedActions;
        private String overallFeedback;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickActionsDto {
        private String mockInterviewUrl;
        private String customJobPostingsUrl;
        private String profileUpdateUrl;
        private String resumeGeneratorUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPreparationAnalysisDto {
        private Integer myScore;
        private Integer averageScore;
        private Integer targetScore;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyProgressDto {
        private String month;
        private Integer applications;
        private Integer interviews;
        private Integer offers;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapabilityDto {
        private String skill;
        private Integer level;
    }
}