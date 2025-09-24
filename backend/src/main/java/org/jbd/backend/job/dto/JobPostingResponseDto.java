package org.jbd.backend.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingResponseDto {
    
    private Long id;
    private Long companyUserId;
    private String companyUserName;
    private String title;
    private String companyName;
    private String location;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private String department;
    private String field;
    private String description;
    private String qualifications;
    private String requiredSkills;
    private String benefits;
    private Integer minSalary;
    private Integer maxSalary;
    private Boolean salaryNegotiable;
    private String workingHours;
    private Boolean isRemotePossible;
    private String contactEmail;
    private String contactPhone;
    private JobStatus status;
    private Long viewCount;
    private Long applicationCount;
    private LocalDate deadlineDate;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static JobPostingResponseDto from(JobPosting jobPosting) {
        // CompanyUser 정보를 안전하게 가져오기
        Long companyUserId = null;
        String companyUserName = null;

        // Hibernate.isInitialized()를 사용하여 프록시가 초기화되었는지 확인
        if (jobPosting.getCompanyUser() != null &&
            Hibernate.isInitialized(jobPosting.getCompanyUser())) {
            companyUserId = jobPosting.getCompanyUser().getId();
            companyUserName = jobPosting.getCompanyUser().getName();
        }

        return JobPostingResponseDto.builder()
                .id(jobPosting.getId())
                .companyUserId(companyUserId)
                .companyUserName(companyUserName)
                .title(jobPosting.getTitle())
                .companyName(jobPosting.getCompanyName())
                .location(jobPosting.getLocation())
                .jobType(jobPosting.getJobType())
                .experienceLevel(jobPosting.getExperienceLevel())
                .department(jobPosting.getDepartment())
                .field(jobPosting.getField())
                .description(jobPosting.getDescription())
                .qualifications(jobPosting.getQualifications())
                .requiredSkills(jobPosting.getRequiredSkills())
                .benefits(jobPosting.getBenefits())
                .minSalary(jobPosting.getSalaryMin())
                .maxSalary(jobPosting.getSalaryMax())
                .salaryNegotiable(jobPosting.getSalaryNegotiable())
                .workingHours(jobPosting.getWorkingHours())
                .isRemotePossible(jobPosting.getIsRemotePossible())
                .contactEmail(jobPosting.getContactEmail())
                .contactPhone(jobPosting.getContactPhone())
                .status(jobPosting.getStatus())
                .viewCount(jobPosting.getViewCount())
                .applicationCount(jobPosting.getApplicationCount())
                .deadlineDate(jobPosting.getDeadlineDate())
                .publishedAt(jobPosting.getPublishedAt())
                .createdAt(jobPosting.getCreatedAt())
                .updatedAt(jobPosting.getUpdatedAt())
                .build();
    }
}