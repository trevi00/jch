package org.jbd.backend.job.repository;

import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
import org.jbd.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUser(User user);

    List<JobApplication> findByJobPosting(JobPosting jobPosting);

    List<JobApplication> findByStatus(ApplicationStatus status);

    Optional<JobApplication> findByUserAndJobPosting(User user, JobPosting jobPosting);

    long countByJobPosting(JobPosting jobPosting);

    List<JobApplication> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<JobApplication> findByJobPostingAndStatus(JobPosting jobPosting, ApplicationStatus status, Pageable pageable);

    Page<JobApplication> findByJobPosting(JobPosting jobPosting, Pageable pageable);

    Page<JobApplication> findByUser(User user, Pageable pageable);

    List<JobApplication> findByUserAndStatus(User user, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    boolean existsByUserAndJobPosting(User user, JobPosting jobPosting);
    
    long countByUser(User user);
    
    long countByUserAndStatus(User user, ApplicationStatus status);
    
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPosting.companyUser = :companyUser")
    long countApplicationsByCompanyUser(@Param("companyUser") User companyUser);
    
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPosting.companyUser = :companyUser AND ja.createdAt >= :fromDate")
    long countApplicationsByCompanyUserAndDateAfter(@Param("companyUser") User companyUser, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPosting.companyUser = :companyUser AND ja.status = :status")
    long countByCompanyUserAndStatus(@Param("companyUser") User companyUser, @Param("status") ApplicationStatus status);
    
    long countByStatusIn(List<ApplicationStatus> statuses);
    
    long countByCreatedAtAfter(LocalDateTime fromDate);
    
    /**
     * 성능 최적화: 기업 사용자의 지원서 상태별 통계를 한 번에 조회
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN ja.status = 'SUBMITTED' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN ja.status = 'DOCUMENT_PASSED' THEN 1 END) as documentPassedCount, " +
           "COUNT(CASE WHEN ja.status = 'INTERVIEW_SCHEDULED' THEN 1 END) as interviewCount, " +
           "COUNT(CASE WHEN ja.status = 'HIRED' THEN 1 END) as hiredCount, " +
           "COUNT(CASE WHEN ja.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "COUNT(*) as totalCount " +
           "FROM JobApplication ja " +
           "WHERE ja.jobPosting.companyUser = :companyUser")
    Object[] findApplicationStatisticsByCompanyUser(@Param("companyUser") User companyUser);
    
    /**
     * 성능 최적화: 일반 사용자의 지원 현황을 한 번에 조회
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN ja.status IN ('SUBMITTED', 'REVIEWED') THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN ja.status IN ('DOCUMENT_PASSED', 'INTERVIEW_SCHEDULED', 'INTERVIEW_PASSED') THEN 1 END) as interviewCount, " +
           "COUNT(CASE WHEN ja.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "COUNT(CASE WHEN ja.status = 'HIRED' THEN 1 END) as acceptedCount, " +
           "COUNT(*) as totalCount " +
           "FROM JobApplication ja " +
           "WHERE ja.user = :user")
    Object[] findApplicationStatisticsByUser(@Param("user") User user);
    
    /**
     * 성능 최적화: 관리자용 전체 지원서 통계를 한 번에 조회
     */
    @Query("SELECT " +
           "COUNT(*) as totalApplications, " +
           "COUNT(CASE WHEN ja.createdAt >= :weekAgo THEN 1 END) as thisWeekApplications, " +
           "COUNT(CASE WHEN ja.status = 'SUBMITTED' THEN 1 END) as pendingApplications, " +
           "COUNT(CASE WHEN ja.status = 'HIRED' THEN 1 END) as successfulApplications " +
           "FROM JobApplication ja")
    Object[] findAdminApplicationStatistics(@Param("weekAgo") LocalDateTime weekAgo);
    
    /**
     * Fetch join을 사용하여 LazyInitializationException 방지
     */
    @Query("SELECT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.user " +
           "LEFT JOIN FETCH ja.jobPosting jp " +
           "LEFT JOIN FETCH jp.companyUser " +
           "WHERE ja.user = :user " +
           "ORDER BY ja.createdAt DESC")
    Page<JobApplication> findByUserWithDetails(@Param("user") User user, Pageable pageable);

    /**
     * Fetch join을 사용하여 기업의 채용공고별 지원자 조회 시 LazyInitializationException 방지
     */
    @Query("SELECT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.user " +
           "LEFT JOIN FETCH ja.jobPosting jp " +
           "LEFT JOIN FETCH jp.companyUser " +
           "WHERE ja.jobPosting = :jobPosting " +
           "ORDER BY ja.createdAt DESC")
    Page<JobApplication> findByJobPostingWithDetails(@Param("jobPosting") JobPosting jobPosting, Pageable pageable);

    /**
     * 월별 지원 현황 조회 (특정 사용자)
     */
    @Query("SELECT " +
           "YEAR(ja.createdAt) as year, " +
           "MONTH(ja.createdAt) as month, " +
           "COUNT(*) as applications, " +
           "COUNT(CASE WHEN ja.status IN ('INTERVIEW_SCHEDULED', 'INTERVIEW_PASSED') THEN 1 END) as interviews, " +
           "COUNT(CASE WHEN ja.status = 'HIRED' THEN 1 END) as offers " +
           "FROM JobApplication ja " +
           "WHERE ja.user = :user AND ja.createdAt >= :startDate " +
           "GROUP BY YEAR(ja.createdAt), MONTH(ja.createdAt) " +
           "ORDER BY YEAR(ja.createdAt) DESC, MONTH(ja.createdAt) DESC")
    List<Object[]> findMonthlyProgressByUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    /**
     * 직무별 취업 통계 조회
     */
    @Query("SELECT " +
           "jp.department as jobField, " +
           "COUNT(*) as totalApplicants, " +
           "COUNT(CASE WHEN ja.status = 'HIRED' THEN 1 END) as employedCount " +
           "FROM JobApplication ja " +
           "JOIN ja.jobPosting jp " +
           "WHERE jp.department IS NOT NULL " +
           "GROUP BY jp.department")
    List<Object[]> findJobFieldEmploymentStatistics();

    /**
     * ID로 지원서 조회 시 연관 엔티티를 함께 fetch하여 LazyInitializationException 방지
     */
    @Query("SELECT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.user " +
           "LEFT JOIN FETCH ja.jobPosting jp " +
           "LEFT JOIN FETCH jp.companyUser " +
           "WHERE ja.id = :id")
    Optional<JobApplication> findByIdWithDetails(@Param("id") Long id);
}