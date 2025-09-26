package org.jbd.backend.job.repository;

import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
import org.jbd.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {
    
    @Override
    @EntityGraph(attributePaths = {"companyUser"})
    Optional<JobPosting> findById(Long id);

    List<JobPosting> findByLocation(String location);

    List<JobPosting> findByJobType(JobType jobType);

    List<JobPosting> findByExperienceLevel(ExperienceLevel experienceLevel);

    List<JobPosting> findByLocationAndJobType(String location, JobType jobType);

    List<JobPosting> findByLocationAndExperienceLevel(String location, ExperienceLevel experienceLevel);

    List<JobPosting> findByJobTypeAndExperienceLevel(JobType jobType, ExperienceLevel experienceLevel);

    List<JobPosting> findByLocationAndJobTypeAndExperienceLevel(String location, JobType jobType, ExperienceLevel experienceLevel);

    @Query("SELECT j FROM JobPosting j WHERE j.title LIKE %:keyword% OR j.description LIKE %:keyword%")
    List<JobPosting> findByTitleContainingOrDescriptionContaining(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"companyUser"})
    List<JobPosting> findByCompanyUser(User companyUser);

    List<JobPosting> findByStatus(JobStatus status);

    @EntityGraph(attributePaths = {"companyUser"})
    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    List<JobPosting> findByStatusAndDeadlineDateBefore(JobStatus status, LocalDate date);

    List<JobPosting> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.location = :location")
    Page<JobPosting> findByStatusAndLocation(@Param("status") JobStatus status, 
                                           @Param("location") String location, 
                                           Pageable pageable);

    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.jobType = :jobType")
    Page<JobPosting> findByStatusAndJobType(@Param("status") JobStatus status, 
                                          @Param("jobType") JobType jobType, 
                                          Pageable pageable);

    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.experienceLevel = :experienceLevel")
    Page<JobPosting> findByStatusAndExperienceLevel(@Param("status") JobStatus status,
                                                   @Param("experienceLevel") ExperienceLevel experienceLevel,
                                                   Pageable pageable);

    // 향상된 필터 검색을 위한 메서드들 (부분 문자열 검색 지원)
    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.location LIKE %:location%")
    Page<JobPosting> findByStatusAndLocationContaining(@Param("status") JobStatus status,
                                                      @Param("location") String location,
                                                      Pageable pageable);

    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status " +
           "AND (:location IS NULL OR j.location LIKE %:location%) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) " +
           "AND (:title IS NULL OR j.title LIKE %:title%) " +
           "AND (:minSalary IS NULL OR j.salaryMin >= :minSalary) " +
           "AND (:maxSalary IS NULL OR j.salaryMax <= :maxSalary)")
    Page<JobPosting> findByFilters(@Param("status") JobStatus status,
                                  @Param("location") String location,
                                  @Param("jobType") JobType jobType,
                                  @Param("experienceLevel") ExperienceLevel experienceLevel,
                                  @Param("title") String title,
                                  @Param("minSalary") Integer minSalary,
                                  @Param("maxSalary") Integer maxSalary,
                                  Pageable pageable);

    long countByCompanyUser(User companyUser);

    long countByStatus(JobStatus status);
    
    long countByCompanyUserAndStatus(User companyUser, JobStatus status);
    
    long countByCreatedAtAfter(LocalDateTime fromDate);
    
    @Query("SELECT j FROM JobPosting j WHERE j.companyUser = :companyUser ORDER BY j.createdAt DESC")
    List<JobPosting> findByCompanyUserOrderByCreatedAtDesc(@Param("companyUser") User companyUser, Pageable pageable);
    
    @Query("SELECT j FROM JobPosting j WHERE j.companyUser = :companyUser ORDER BY j.viewCount DESC")
    List<JobPosting> findByCompanyUserOrderByViewCountDesc(@Param("companyUser") User companyUser, Pageable pageable);
    
    /**
     * 성능 최적화: 채용공고와 지원자 수를 한 번에 조회 (N+1 문제 해결)
     */
    @Query("SELECT j, COALESCE(COUNT(ja), 0) as applicationCount " +
           "FROM JobPosting j " +
           "LEFT JOIN JobApplication ja ON j.id = ja.jobPosting.id " +
           "WHERE j.companyUser = :companyUser " +
           "GROUP BY j " +
           "ORDER BY j.viewCount DESC")
    List<Object[]> findByCompanyUserWithApplicationCountOrderByViewCountDesc(
            @Param("companyUser") User companyUser, Pageable pageable);
    
    /**
     * 성능 최적화: 최신 채용공고와 지원자 수를 한 번에 조회
     */
    @Query("SELECT j, COALESCE(COUNT(ja), 0) as applicationCount " +
           "FROM JobPosting j " +
           "LEFT JOIN JobApplication ja ON j.id = ja.jobPosting.id " +
           "WHERE j.companyUser = :companyUser " +
           "GROUP BY j " +
           "ORDER BY j.createdAt DESC")
    List<Object[]> findByCompanyUserWithApplicationCountOrderByCreatedAtDesc(
            @Param("companyUser") User companyUser, Pageable pageable);
    
    /**
     * 성능 최적화: 관리자용 채용공고 통계를 한 번에 조회
     */
    @Query("SELECT " +
           "COUNT(*) as totalJobPostings, " +
           "COUNT(CASE WHEN jp.status = org.jbd.backend.job.domain.enums.JobStatus.PUBLISHED THEN 1 END) as activeJobPostings, " +
           "COUNT(CASE WHEN jp.status = org.jbd.backend.job.domain.enums.JobStatus.CLOSED THEN 1 END) as closedJobPostings, " +
           "COUNT(CASE WHEN jp.createdAt >= :weekAgo THEN 1 END) as thisWeekJobPostings " +
           "FROM JobPosting jp")
    Object[] findJobPostingStatistics(@Param("weekAgo") LocalDateTime weekAgo);

    /**
     * 마감 임박 채용공고 조회 (N일 이내)
     */
    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.status = org.jbd.backend.job.domain.enums.JobStatus.PUBLISHED " +
           "AND j.deadlineDate BETWEEN :now AND :deadline " +
           "ORDER BY j.deadlineDate ASC")
    List<JobPosting> findDeadlineApproachingJobPostings(@Param("now") LocalDate now,
                                                       @Param("deadline") LocalDate deadline);

    /**
     * 기업별 마감 임박 채용공고 조회
     */
    @EntityGraph(attributePaths = {"companyUser"})
    @Query("SELECT j FROM JobPosting j WHERE j.companyUser = :companyUser " +
           "AND j.status = org.jbd.backend.job.domain.enums.JobStatus.PUBLISHED " +
           "AND j.deadlineDate BETWEEN :now AND :deadline " +
           "ORDER BY j.deadlineDate ASC")
    List<JobPosting> findDeadlineApproachingJobPostingsByCompany(@Param("companyUser") User companyUser,
                                                               @Param("now") LocalDate now,
                                                               @Param("deadline") LocalDate deadline);
}