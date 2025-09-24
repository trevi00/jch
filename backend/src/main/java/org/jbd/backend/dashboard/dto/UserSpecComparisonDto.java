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
public class UserSpecComparisonDto {

    private String companyName;
    private Integer totalApplicants;
    private UserSpecDto mySpec;
    private UserSpecDto averageSpec;
    private ComparisonResultDto comparisonResult;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSpecDto {
        private Integer skillCount;
        private Integer certificationCount;
        private Integer portfolioCount;
        private Integer experienceMonths;
        private String educationLevel;
        private Double gpa;
        private Integer jobScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonResultDto {
        private String overallRanking; // "상위 20%", "평균", "하위 30%" 등
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> improvements;
        private Double competitivenessScore; // 0-100점
    }
}