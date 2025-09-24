package org.jbd.backend.job.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
import org.jbd.backend.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_id"}))
public class JobApplication extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting jobPosting;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;
    
    @Column(name = "cover_letter", length = 2000)
    private String coverLetter;
    
    @Column(name = "resume_url")
    private String resumeUrl;
    
    @Column(name = "portfolio_urls", length = 1000)
    private String portfolioUrls;
    
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "interviewer_notes", length = 1000)
    private String interviewerNotes;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "interview_scheduled_at")
    private LocalDateTime interviewScheduledAt;
    
    @Column(name = "final_decision_at")
    private LocalDateTime finalDecisionAt;
    
    protected JobApplication() {}
    
    public JobApplication(User user, JobPosting jobPosting, String coverLetter) {
        this.user = user;
        this.jobPosting = jobPosting;
        this.coverLetter = coverLetter;
        this.status = ApplicationStatus.SUBMITTED;
        this.appliedAt = LocalDateTime.now();
    }
    
    public void updateApplication(String coverLetter, String resumeUrl, String portfolioUrls) {
        if (this.status == ApplicationStatus.SUBMITTED) {
            this.coverLetter = coverLetter;
            this.resumeUrl = resumeUrl;
            this.portfolioUrls = portfolioUrls;
        }
    }
    
    public void review() {
        if (this.status == ApplicationStatus.SUBMITTED) {
            this.status = ApplicationStatus.REVIEWED;
            this.reviewedAt = LocalDateTime.now();
        }
    }
    
    public void passDocumentReview() {
        if (this.status == ApplicationStatus.SUBMITTED || this.status == ApplicationStatus.REVIEWED) {
            this.status = ApplicationStatus.DOCUMENT_PASSED;
            if (this.reviewedAt == null) {
                this.reviewedAt = LocalDateTime.now();
            }
        }
    }
    
    public void scheduleInterview(LocalDateTime interviewTime) {
        if (this.status == ApplicationStatus.DOCUMENT_PASSED) {
            this.status = ApplicationStatus.INTERVIEW_SCHEDULED;
            this.interviewScheduledAt = interviewTime;
        }
    }
    
    public void passInterview() {
        if (this.status == ApplicationStatus.DOCUMENT_PASSED || 
            this.status == ApplicationStatus.INTERVIEW_SCHEDULED) {
            this.status = ApplicationStatus.INTERVIEW_PASSED;
        }
    }
    
    public void hire() {
        if (this.status == ApplicationStatus.INTERVIEW_PASSED) {
            this.status = ApplicationStatus.HIRED;
            this.finalDecisionAt = LocalDateTime.now();
        }
    }
    
    public void reject(String rejectionReason) {
        if (this.status != ApplicationStatus.HIRED) {
            this.status = ApplicationStatus.REJECTED;
            this.rejectionReason = rejectionReason;
            this.finalDecisionAt = LocalDateTime.now();
        }
    }
    
    public void withdraw() {
        if (this.status != ApplicationStatus.HIRED && 
            this.status != ApplicationStatus.REJECTED) {
            this.status = ApplicationStatus.WITHDRAWN;
        }
    }
    
    public void addInterviewerNotes(String notes) {
        this.interviewerNotes = notes;
    }
    
    public boolean canBeWithdrawn() {
        return this.status == ApplicationStatus.SUBMITTED || 
               this.status == ApplicationStatus.REVIEWED ||
               this.status == ApplicationStatus.DOCUMENT_PASSED ||
               this.status == ApplicationStatus.INTERVIEW_SCHEDULED;
    }
    
    public boolean canBeModified() {
        return this.status == ApplicationStatus.SUBMITTED;
    }
    
    public boolean isInProgress() {
        return this.status != ApplicationStatus.REJECTED && 
               this.status != ApplicationStatus.HIRED &&
               this.status != ApplicationStatus.WITHDRAWN;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public JobPosting getJobPosting() {
        return jobPosting;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public String getCoverLetter() {
        return coverLetter;
    }
    
    public String getResumeUrl() {
        return resumeUrl;
    }
    
    public String getPortfolioUrls() {
        return portfolioUrls;
    }
    
    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public String getInterviewerNotes() {
        return interviewerNotes;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public LocalDateTime getInterviewScheduledAt() {
        return interviewScheduledAt;
    }
    
    public LocalDateTime getFinalDecisionAt() {
        return finalDecisionAt;
    }
}