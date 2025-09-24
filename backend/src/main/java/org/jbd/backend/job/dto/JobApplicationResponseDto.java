package org.jbd.backend.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponseDto {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long jobPostingId;
    private String jobPostingTitle;
    private String companyName;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
    private String portfolioUrls;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String interviewerNotes;
    private String rejectionReason;
    private LocalDateTime interviewScheduledAt;
    private LocalDateTime finalDecisionAt;
    
    public static JobApplicationResponseDto from(JobApplication application) {
        return JobApplicationResponseDto.builder()
                .id(application.getId())
                .userId(application.getUser().getId())
                .userName(application.getUser().getName()) // User 엔티티의 임시 getName() 메서드 사용
                .userEmail(application.getUser().getEmail())
                .jobPostingId(application.getJobPosting().getId())
                .jobPostingTitle(application.getJobPosting().getTitle())
                .companyName(application.getJobPosting().getCompanyName())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .portfolioUrls(application.getPortfolioUrls())
                .appliedAt(application.getAppliedAt())
                .reviewedAt(application.getReviewedAt())
                .interviewerNotes(application.getInterviewerNotes())
                .rejectionReason(application.getRejectionReason())
                .interviewScheduledAt(application.getInterviewScheduledAt())
                .finalDecisionAt(application.getFinalDecisionAt())
                .build();
    }
}