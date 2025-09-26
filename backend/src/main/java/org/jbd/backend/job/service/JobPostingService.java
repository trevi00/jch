package org.jbd.backend.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
import org.jbd.backend.job.dto.JobPostingResponseDto;
import org.jbd.backend.job.dto.JobPostingSearchDto;
import org.jbd.backend.job.dto.JobPostingStatsDto;
import org.jbd.backend.job.repository.JobApplicationRepository;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.job.specification.JobPostingSpecification;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobPosting createJobPosting(Long userId, String title, String companyName, String location,
                                      JobType jobType, ExperienceLevel experienceLevel,
                                      String description, Integer minSalary, Integer maxSalary) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobPosting jobPosting = new JobPosting(user, title, companyName, location, jobType, experienceLevel);
        jobPosting.updateContent(description, null, null, null);
        jobPosting.updateSalaryInfo(minSalary, maxSalary, false);

        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public JobPosting publishJobPosting(Long jobPostingId, LocalDate deadlineDate) {
        JobPosting jobPosting = getJobPosting(jobPostingId);
        jobPosting.publish(deadlineDate);
        return jobPostingRepository.save(jobPosting);
    }

    public JobPosting getJobPosting(Long jobPostingId) {
        return jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
    }

    public Page<JobPosting> getPublishedJobPostings(Pageable pageable) {
        return jobPostingRepository.findByStatus(JobStatus.PUBLISHED, pageable);
    }

    public List<JobPosting> searchJobPostingsByLocation(String location) {
        return jobPostingRepository.findByLocation(location);
    }

    public List<JobPosting> searchJobPostingsByJobType(JobType jobType) {
        return jobPostingRepository.findByJobType(jobType);
    }

    public List<JobPosting> searchJobPostingsByExperienceLevel(ExperienceLevel experienceLevel) {
        return jobPostingRepository.findByExperienceLevel(experienceLevel);
    }

    public List<JobPosting> searchJobPostingsByKeyword(String keyword) {
        return jobPostingRepository.findByTitleContainingOrDescriptionContaining(keyword);
    }

    @Transactional
    public void incrementViewCount(Long jobPostingId) {
        JobPosting jobPosting = getJobPosting(jobPostingId);
        jobPosting.incrementViewCount();
        jobPostingRepository.save(jobPosting);
    }

    public List<JobPosting> getJobPostingsByCompanyUser(User companyUser) {
        return jobPostingRepository.findByCompanyUser(companyUser);
    }

    @Transactional
    public JobPosting updateJobPosting(Long jobPostingId, String title, String description,
                                      Integer minSalary, Integer maxSalary) {
        JobPosting jobPosting = getJobPosting(jobPostingId);
        jobPosting.updateBasicInfo(title, jobPosting.getCompanyName(), jobPosting.getLocation(),
                                   jobPosting.getJobType(), jobPosting.getDepartment(), jobPosting.getField(),
                                   jobPosting.getExperienceLevel());
        jobPosting.updateContent(description, jobPosting.getQualifications(), jobPosting.getRequiredSkills(), jobPosting.getBenefits());
        jobPosting.updateSalaryInfo(minSalary, maxSalary, jobPosting.getSalaryNegotiable());
        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public JobPosting updateJobPosting(Long jobPostingId, org.jbd.backend.job.dto.JobPostingUpdateDto dto) {
        JobPosting jobPosting = getJobPosting(jobPostingId);

        // 기본 정보 업데이트 (null이 아닌 필드만)
        if (dto.getTitle() != null || dto.getCompanyName() != null || dto.getLocation() != null ||
            dto.getJobType() != null || dto.getDepartment() != null || dto.getField() != null ||
            dto.getExperienceLevel() != null) {
            jobPosting.updateBasicInfo(
                dto.getTitle() != null ? dto.getTitle() : jobPosting.getTitle(),
                dto.getCompanyName() != null ? dto.getCompanyName() : jobPosting.getCompanyName(),
                dto.getLocation() != null ? dto.getLocation() : jobPosting.getLocation(),
                dto.getJobType() != null ? dto.getJobType() : jobPosting.getJobType(),
                dto.getDepartment() != null ? dto.getDepartment() : jobPosting.getDepartment(),
                dto.getField() != null ? dto.getField() : jobPosting.getField(),
                dto.getExperienceLevel() != null ? dto.getExperienceLevel() : jobPosting.getExperienceLevel()
            );
        }

        // 내용 정보 업데이트
        if (dto.getDescription() != null || dto.getQualifications() != null ||
            dto.getRequiredSkills() != null || dto.getBenefits() != null) {
            jobPosting.updateContent(
                dto.getDescription() != null ? dto.getDescription() : jobPosting.getDescription(),
                dto.getQualifications() != null ? dto.getQualifications() : jobPosting.getQualifications(),
                dto.getRequiredSkills() != null ? dto.getRequiredSkills() : jobPosting.getRequiredSkills(),
                dto.getBenefits() != null ? dto.getBenefits() : jobPosting.getBenefits()
            );
        }

        // 급여 정보 업데이트
        if (dto.getMinSalary() != null || dto.getMaxSalary() != null || dto.getSalaryNegotiable() != null) {
            jobPosting.updateSalaryInfo(
                dto.getMinSalary() != null ? dto.getMinSalary() : jobPosting.getSalaryMin(),
                dto.getMaxSalary() != null ? dto.getMaxSalary() : jobPosting.getSalaryMax(),
                dto.getSalaryNegotiable() != null ? dto.getSalaryNegotiable() : jobPosting.getSalaryNegotiable()
            );
        }

        // 근무 조건 업데이트
        if (dto.getWorkingHours() != null || dto.getIsRemotePossible() != null) {
            jobPosting.updateWorkingConditions(
                dto.getWorkingHours() != null ? dto.getWorkingHours() : jobPosting.getWorkingHours(),
                dto.getIsRemotePossible() != null ? dto.getIsRemotePossible() : jobPosting.getIsRemotePossible()
            );
        }

        // 연락처 정보 업데이트
        if (dto.getContactEmail() != null || dto.getContactPhone() != null) {
            jobPosting.updateContactInfo(
                dto.getContactEmail() != null ? dto.getContactEmail() : jobPosting.getContactEmail(),
                dto.getContactPhone() != null ? dto.getContactPhone() : jobPosting.getContactPhone()
            );
        }

        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public JobPosting closeJobPosting(Long jobPostingId) {
        JobPosting jobPosting = getJobPosting(jobPostingId);
        jobPosting.close();
        return jobPostingRepository.save(jobPosting);
    }

    public List<JobPosting> getExpiredJobPostings() {
        return jobPostingRepository.findByStatusAndDeadlineDateBefore(JobStatus.PUBLISHED, LocalDate.now());
    }

    public Page<JobPosting> searchJobPostings(String location, JobType jobType, ExperienceLevel experienceLevel, Pageable pageable) {
        // 향상된 필터 검색 사용 (부분 문자열 검색 지원)
        return jobPostingRepository.findByFilters(
            JobStatus.PUBLISHED,
            location,
            jobType,
            experienceLevel,
            null,  // title
            null,  // minSalary
            null,  // maxSalary
            pageable
        );
    }

    // 향상된 검색 메서드 추가 (제목, 연봉 범위 포함)
    public Page<JobPosting> searchJobPostingsAdvanced(String title, String location, JobType jobType,
                                                     ExperienceLevel experienceLevel, Integer minSalary,
                                                     Integer maxSalary, Pageable pageable) {
        return jobPostingRepository.findByFilters(
            JobStatus.PUBLISHED,
            location,
            jobType,
            experienceLevel,
            title,
            minSalary,
            maxSalary,
            pageable
        );
    }

    public long getJobPostingCountByCompanyUser(User companyUser) {
        return jobPostingRepository.countByCompanyUser(companyUser);
    }

    @Transactional
    public void deleteJobPosting(Long jobPostingId) {
        JobPosting jobPosting = getJobPosting(jobPostingId);

        // 먼저 연관된 지원서들을 삭제
        List<JobApplication> applications = jobApplicationRepository.findByJobPosting(jobPosting);
        if (!applications.isEmpty()) {
            jobApplicationRepository.deleteAll(applications);
        }

        // 그 다음 채용공고 삭제
        jobPostingRepository.delete(jobPosting);
    }

    // ====== JobAtda 통합 기능들 ======

    /**
     * 고급 검색 기능 (JPA Specification 활용)
     */
    public Page<JobPosting> searchJobPostingsWithSpecification(JobPostingSearchDto searchDto, Pageable pageable) {
        log.info("Advanced search with criteria: {}", searchDto);

        Specification<JobPosting> spec = JobPostingSpecification.withSearchCriteria(searchDto);
        return jobPostingRepository.findAll(spec, pageable);
    }

    /**
     * 간편 검색 기능 (키워드 + 기본 필터)
     */
    @Transactional(readOnly = true)
    public Page<JobPosting> searchJobPostingsSimple(String keyword, String location, JobType jobType,
                                                   ExperienceLevel experienceLevel, Pageable pageable) {
        log.info("Simple search - keyword: {}, location: {}, jobType: {}, experienceLevel: {}",
                keyword, location, jobType, experienceLevel);

        // @EntityGraph가 적용된 Repository 메서드를 직접 사용
        Page<JobPosting> result = jobPostingRepository.findByFilters(
            JobStatus.PUBLISHED,
            location,
            jobType,
            experienceLevel,
            keyword,  // title로 키워드 전달
            null,     // minSalary
            null,     // maxSalary
            pageable
        );

        // 디버깅: CompanyUser가 로드되었는지 확인
        result.getContent().forEach(jp -> {
            log.debug("JobPosting ID: {}, CompanyUser initialized: {}",
                jp.getId(),
                jp.getCompanyUser() != null ? Hibernate.isInitialized(jp.getCompanyUser()) : "null");
        });

        return result;
    }

    /**
     * 간편 검색 기능 - DTO 포함 버전
     */
    @Transactional(readOnly = true)
    public Page<JobPostingResponseDto> searchJobPostingsSimpleWithDto(String keyword, String location, JobType jobType,
                                                                     ExperienceLevel experienceLevel, Pageable pageable) {
        log.info("Simple search with DTO - keyword: {}, location: {}, jobType: {}, experienceLevel: {}",
                keyword, location, jobType, experienceLevel);

        // @EntityGraph가 적용된 Repository 메서드를 직접 사용
        Page<JobPosting> result = jobPostingRepository.findByFilters(
            JobStatus.PUBLISHED,
            location,
            jobType,
            experienceLevel,
            keyword,  // title로 키워드 전달
            null,     // minSalary
            null,     // maxSalary
            pageable
        );

        // 트랜잭션 내에서 DTO 변환
        return result.map(JobPostingResponseDto::from);
    }

    /**
     * 마감 임박 채용공고 조회
     */
    public List<JobPosting> getDeadlineApproachingJobPostings(int days) {
        log.info("Fetching job postings with deadline approaching in {} days", days);

        if (days <= 0 || days > 30) {
            throw new IllegalArgumentException("일수는 1일에서 30일 사이여야 합니다.");
        }

        LocalDate now = LocalDate.now();
        LocalDate deadline = now.plusDays(days);

        return jobPostingRepository.findDeadlineApproachingJobPostings(now, deadline);
    }

    /**
     * 기업별 마감 임박 채용공고 조회
     */
    public List<JobPosting> getDeadlineApproachingJobPostingsByCompany(User companyUser, int days) {
        log.info("Fetching deadline approaching job postings for company: {}, days: {}",
                companyUser.getId(), days);

        if (days <= 0 || days > 30) {
            throw new IllegalArgumentException("일수는 1일에서 30일 사이여야 합니다.");
        }

        LocalDate now = LocalDate.now();
        LocalDate deadline = now.plusDays(days);

        return jobPostingRepository.findDeadlineApproachingJobPostingsByCompany(companyUser, now, deadline);
    }

    /**
     * 채용공고 상세 통계 조회
     */
    public JobPostingStatsDto getJobPostingStats(Long jobPostingId) {
        log.info("Fetching job posting statistics for id: {}", jobPostingId);

        JobPosting jobPosting = getJobPosting(jobPostingId);

        return convertToStatsDto(jobPosting);
    }

    /**
     * 기업별 채용공고 통계 목록 조회
     */
    public List<JobPostingStatsDto> getJobPostingStatsByCompany(User companyUser) {
        log.info("Fetching job posting statistics for company: {}", companyUser.getId());

        List<JobPosting> jobPostings = jobPostingRepository.findByCompanyUser(companyUser);

        return jobPostings.stream()
                .map(this::convertToStatsDto)
                .collect(Collectors.toList());
    }

    /**
     * 전체 채용공고 통계 (관리자용)
     */
    public Object getOverallJobPostingStatistics() {
        log.info("Fetching overall job posting statistics");

        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Object[] stats = jobPostingRepository.findJobPostingStatistics(weekAgo);

        return Map.of(
            "totalJobPostings", stats[0],
            "activeJobPostings", stats[1],
            "closedJobPostings", stats[2],
            "thisWeekJobPostings", stats[3]
        );
    }

    /**
     * 관리자용 - 모든 채용공고 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<JobPostingResponseDto> getAllJobPostings(Pageable pageable) {
        log.info("Admin request - get all job postings with pagination");

        Page<JobPosting> result = jobPostingRepository.findAll(pageable);

        // 트랜잭션 내에서 DTO 변환
        return result.map(JobPostingResponseDto::from);
    }

    /**
     * JobPosting을 JobPostingStatsDto로 변환
     */
    private JobPostingStatsDto convertToStatsDto(JobPosting jobPosting) {
        Long daysUntilDeadline = null;
        Boolean isDeadlineApproaching = false;
        Boolean isExpired = false;

        if (jobPosting.getDeadlineDate() != null) {
            LocalDate now = LocalDate.now();
            daysUntilDeadline = ChronoUnit.DAYS.between(now, jobPosting.getDeadlineDate());
            isDeadlineApproaching = daysUntilDeadline <= 7 && daysUntilDeadline >= 0;
            isExpired = daysUntilDeadline < 0;
        }

        Double viewToApplicationRatio = null;
        if (jobPosting.getViewCount() > 0) {
            viewToApplicationRatio = (double) jobPosting.getApplicationCount() / jobPosting.getViewCount() * 100;
        }

        return JobPostingStatsDto.builder()
                .jobId(jobPosting.getId())
                .title(jobPosting.getTitle())
                .companyName(jobPosting.getCompanyName())
                .viewCount(jobPosting.getViewCount())
                .applicationCount(jobPosting.getApplicationCount())
                .status(jobPosting.getStatus())
                .publishedAt(jobPosting.getPublishedAt())
                .deadlineDate(jobPosting.getDeadlineDate())
                .createdAt(jobPosting.getCreatedAt())
                .updatedAt(jobPosting.getUpdatedAt())
                .daysUntilDeadline(daysUntilDeadline)
                .viewToApplicationRatio(viewToApplicationRatio)
                .isDeadlineApproaching(isDeadlineApproaching)
                .isExpired(isExpired)
                .isActive(jobPosting.getStatus() == JobStatus.PUBLISHED)
                .build();
    }
}