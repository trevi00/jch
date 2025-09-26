package org.jbd.backend.job.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.job.domain.enums.JobType;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 채용공고 도메인 엔티티
 *
 * 기업이 등록하는 채용공고 정보를 관리하는 핵심 도메인 객체입니다.
 * 채용공고의 전체 생명주기(작성, 발행, 지원, 마감)를 지원하며
 * 지원자와 기업 간의 매칭을 위한 다양한 정보를 포함합니다.
 *
 * 도메인 관계:
 * - User (N:1): 채용공고를 등록한 기업 사용자
 * - JobApplication (1:N): 해당 채용공고에 대한 지원서들
 *
 * 주요 생명주기:
 * 1. DRAFT: 작성 중인 임시 저장 상태
 * 2. PUBLISHED: 발행되어 지원 가능한 상태
 * 3. CLOSED: 마감되어 지원 불가능한 상태
 *
 * 비즈니스 규칙:
 * - 기업 사용자(UserType.COMPANY)만 채용공고를 등록할 수 있음
 * - 발행 시 마감일이 설정되어야 함
 * - 마감일이 지나면 자동으로 지원 불가능 상태가 됨
 * - 조회수와 지원수가 자동으로 집계됨
 * - 급여 정보는 협의 가능으로 설정할 수 있음
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see User
 * @see JobApplication
 * @see JobType
 * @see ExperienceLevel
 * @see JobStatus
 */
@Entity
@Table(name = "job_postings")
public class    JobPosting extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_user_id", nullable = false)
    private User companyUser;  // 기업 유저만 생성 가능
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "location", nullable = false)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "field")
    private String field;
    
    @Column(name = "salary_min")
    private Integer salaryMin;
    
    @Column(name = "salary_max")
    private Integer salaryMax;
    
    @Column(name = "salary_negotiable")
    private Boolean salaryNegotiable = false;
    
    @Column(name = "description", length = 3000)
    private String description;
    
    @Column(name = "qualifications", length = 2000)
    private String qualifications;  // 지원 자격 리스트
    
    @Column(name = "required_skills", length = 1500)
    private String requiredSkills;  // 필요 기술 리스트
    
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;
    
    @Column(name = "deadline_date")
    private LocalDate deadlineDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;
    
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @Column(name = "application_count", nullable = false)
    private Long applicationCount = 0L;
    
    @Column(name = "benefits", length = 1000)
    private String benefits;
    
    @Column(name = "working_hours")
    private String workingHours;
    
    @Column(name = "is_remote_possible")
    private Boolean isRemotePossible = false;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    protected JobPosting() {}
    
    public JobPosting(User companyUser, String title, String companyName, String location, 
                     JobType jobType, ExperienceLevel experienceLevel) {
        this.companyUser = companyUser;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.status = JobStatus.DRAFT;
    }
    
    public void updateBasicInfo(String title, String companyName, String location, JobType jobType,
                               String department, String field, ExperienceLevel experienceLevel) {
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.jobType = jobType;
        this.department = department;
        this.field = field;
        this.experienceLevel = experienceLevel;
    }
    
    public void updateSalaryInfo(Integer salaryMin, Integer salaryMax, Boolean salaryNegotiable) {
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryNegotiable = salaryNegotiable;
    }
    
    public void updateContent(String description, String qualifications, String requiredSkills, String benefits) {
        this.description = description;
        this.qualifications = qualifications;
        this.requiredSkills = requiredSkills;
        this.benefits = benefits;
    }
    
    public void updateWorkingConditions(String workingHours, Boolean isRemotePossible) {
        this.workingHours = workingHours;
        this.isRemotePossible = isRemotePossible;
    }
    
    public void updateContactInfo(String contactEmail, String contactPhone) {
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }
    
    public void publish(LocalDate deadlineDate) {
        this.status = JobStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.deadlineDate = deadlineDate;
    }
    
    public void close() {
        this.status = JobStatus.CLOSED;
    }
    
    public void reopen() {
        if (this.status == JobStatus.CLOSED) {
            this.status = JobStatus.PUBLISHED;
        }
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementApplicationCount() {
        this.applicationCount++;
    }
    
    public void decrementApplicationCount() {
        if (this.applicationCount > 0) {
            this.applicationCount--;
        }
    }
    
    public boolean isPublished() {
        return this.status == JobStatus.PUBLISHED;
    }
    
    public boolean isClosed() {
        return this.status == JobStatus.CLOSED;
    }
    
    public boolean isDraft() {
        return this.status == JobStatus.DRAFT;
    }
    
    public boolean isExpired() {
        return this.deadlineDate != null && LocalDate.now().isAfter(this.deadlineDate);
    }
    
    public boolean canApply() {
        return isPublished() && !isExpired();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getCompanyUser() {
        return companyUser;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public JobType getJobType() {
        return jobType;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public String getField() {
        return field;
    }
    
    public Integer getSalaryMin() {
        return salaryMin;
    }
    
    public Integer getSalaryMax() {
        return salaryMax;
    }
    
    public Boolean getSalaryNegotiable() {
        return salaryNegotiable;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getQualifications() {
        return qualifications;
    }
    
    public String getRequiredSkills() {
        return requiredSkills;
    }
    
    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }
    
    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public Long getApplicationCount() {
        return applicationCount;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public String getWorkingHours() {
        return workingHours;
    }
    
    public Boolean getIsRemotePossible() {
        return isRemotePossible;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}