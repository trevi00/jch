package org.jbd.backend.company.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.company.domain.Company;
import org.jbd.backend.company.dto.CompanyDashboardDto;
import org.jbd.backend.company.repository.CompanyRepository;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.repository.JobApplicationRepository;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 기업 대시보드 서비스
 *
 * 기업의 채용 현황을 종합적으로 분석하여 대시보드에 필요한
 * 통계 데이터와 최신 정보를 제공하는 서비스입니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyDashboardService {

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    /**
     * 기업 대시보드 종합 정보 조회
     *
     * @param userId 기업 사용자 ID
     * @return 대시보드 종합 정보
     */
    public CompanyDashboardDto getCompanyDashboard(Long userId) {
        log.info("Fetching company dashboard for user: {}", userId);

        // 기업 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Company company = companyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("기업 정보를 찾을 수 없습니다"));

        // 각 섹션별 데이터 조회
        CompanyDashboardDto.CompanyInfo companyInfo = buildCompanyInfo(company);
        CompanyDashboardDto.HiringStatistics hiringStats = buildHiringStatistics(company);
        List<CompanyDashboardDto.RecentApplicant> recentApplicants = buildRecentApplicants(company);
        List<CompanyDashboardDto.JobPostingSummary> recentJobPostings = buildRecentJobPostings(company);
        CompanyDashboardDto.NotificationSummary notifications = buildNotificationSummary(company);

        return CompanyDashboardDto.builder()
                .companyInfo(companyInfo)
                .hiringStats(hiringStats)
                .recentApplicants(recentApplicants)
                .recentJobPostings(recentJobPostings)
                .notifications(notifications)
                .build();
    }

    /**
     * 기업 기본 정보 구성
     */
    private CompanyDashboardDto.CompanyInfo buildCompanyInfo(Company company) {
        return CompanyDashboardDto.CompanyInfo.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .industry(company.getIndustry())
                .location(company.getLocation())
                .logoUrl(company.getLogoUrl())
                .isVerified(company.getIsVerified() != null ? company.getIsVerified() : false)
                .build();
    }

    /**
     * 채용 통계 정보 구성
     */
    private CompanyDashboardDto.HiringStatistics buildHiringStatistics(Company company) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        User companyUser = company.getUser();

        // 기본 통계 조회 (User 기반으로 조회)
        Integer totalJobPostings = (int) jobPostingRepository.countByCompanyUser(companyUser);
        Integer activeJobPostings = totalJobPostings; // 활성 공고 = 삭제되지 않은 공고

        // 지원자 통계 - 회사별 지원서 조회
        List<JobApplication> allApplications = new ArrayList<>();
        List<JobPosting> companyJobPostings = jobPostingRepository.findByCompanyUser(companyUser);
        for (JobPosting jobPosting : companyJobPostings) {
            allApplications.addAll(jobApplicationRepository.findByJobPosting(jobPosting));
        }

        Integer totalApplicants = allApplications.size();
        Integer newApplicantsToday = (int) allApplications.stream()
                .filter(app -> app.getAppliedAt().isAfter(todayStart))
                .count();
        Integer newApplicantsThisWeek = (int) allApplications.stream()
                .filter(app -> app.getAppliedAt().isAfter(weekStart))
                .count();

        // 상태별 통계
        Integer pendingReviewCount = (int) allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SUBMITTED ||
                             app.getStatus() == ApplicationStatus.REVIEWED)
                .count();
        Integer interviewScheduledCount = (int) allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED)
                .count();
        Integer hiredThisMonth = (int) allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.HIRED &&
                             app.getUpdatedAt().isAfter(monthStart))
                .count();

        // 평균 지원율 계산 (조회수 대비 지원 비율)
        Double averageApplicationRate = companyJobPostings.stream()
                .filter(job -> job.getViewCount() > 0)
                .mapToDouble(job -> (double) job.getApplicationCount() / job.getViewCount() * 100)
                .average()
                .orElse(0.0);

        return CompanyDashboardDto.HiringStatistics.builder()
                .totalJobPostings(totalJobPostings)
                .activeJobPostings(activeJobPostings)
                .totalApplicants(totalApplicants)
                .newApplicantsToday(newApplicantsToday)
                .newApplicantsThisWeek(newApplicantsThisWeek)
                .pendingReviewCount(pendingReviewCount)
                .interviewScheduledCount(interviewScheduledCount)
                .hiredThisMonth(hiredThisMonth)
                .averageApplicationRate(Math.round(averageApplicationRate * 100.0) / 100.0)
                .build();
    }

    /**
     * 최근 지원자 정보 구성
     */
    private List<CompanyDashboardDto.RecentApplicant> buildRecentApplicants(Company company) {
        User companyUser = company.getUser();
        List<JobPosting> companyJobPostings = jobPostingRepository.findByCompanyUser(companyUser);

        // 최근 지원자 5명 조회
        List<JobApplication> allApplicationsForRecent = new ArrayList<>();
        for (JobPosting jobPosting : companyJobPostings) {
            allApplicationsForRecent.addAll(jobApplicationRepository.findByJobPosting(jobPosting));
        }
        List<JobApplication> recentApplications = allApplicationsForRecent.stream()
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .limit(5)
                .collect(Collectors.toList());

        return recentApplications.stream()
                .map(this::mapToRecentApplicant)
                .collect(Collectors.toList());
    }

    /**
     * JobApplication을 RecentApplicant DTO로 변환
     */
    private CompanyDashboardDto.RecentApplicant mapToRecentApplicant(JobApplication application) {
        String coverLetterPreview = application.getCoverLetter() != null && !application.getCoverLetter().isEmpty()
                ? (application.getCoverLetter().length() > 100
                   ? application.getCoverLetter().substring(0, 100) + "..."
                   : application.getCoverLetter())
                : "자기소개서가 없습니다.";

        return CompanyDashboardDto.RecentApplicant.builder()
                .applicationId(application.getId())
                .applicantId(application.getUser().getId())
                .applicantName(application.getUser().getName())
                .applicantEmail(application.getUser().getEmail())
                .jobPostingId(application.getJobPosting().getId())
                .jobTitle(application.getJobPosting().getTitle())
                .status(application.getStatus().name())
                .appliedAt(application.getAppliedAt())
                .coverLetterPreview(coverLetterPreview)
                .build();
    }

    /**
     * 최근 채용공고 요약 정보 구성
     */
    private List<CompanyDashboardDto.JobPostingSummary> buildRecentJobPostings(Company company) {
        User companyUser = company.getUser();
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<JobPosting> recentJobPostings = jobPostingRepository
                .findByCompanyUserOrderByCreatedAtDesc(companyUser, pageable);

        return recentJobPostings.stream()
                .map(this::mapToJobPostingSummary)
                .collect(Collectors.toList());
    }

    /**
     * JobPosting을 JobPostingSummary DTO로 변환
     */
    private CompanyDashboardDto.JobPostingSummary mapToJobPostingSummary(JobPosting jobPosting) {
        LocalDateTime now = LocalDateTime.now();
        boolean isDeadlineApproaching = false;
        Integer daysUntilDeadline = null;

        if (jobPosting.getDeadlineDate() != null) {
            daysUntilDeadline = (int) ChronoUnit.DAYS.between(now.toLocalDate(), jobPosting.getDeadlineDate());
            isDeadlineApproaching = daysUntilDeadline <= 7 && daysUntilDeadline >= 0;
        }

        return CompanyDashboardDto.JobPostingSummary.builder()
                .jobPostingId(jobPosting.getId())
                .title(jobPosting.getTitle())
                .status(jobPosting.isClosed() ? "CLOSED" : "PUBLISHED")
                .viewCount(Math.toIntExact(jobPosting.getViewCount()))
                .applicationCount(Math.toIntExact(jobPosting.getApplicationCount()))
                .createdAt(jobPosting.getCreatedAt())
                .deadline(jobPosting.getDeadlineDate() != null ? jobPosting.getDeadlineDate().atStartOfDay() : null)
                .isDeadlineApproaching(isDeadlineApproaching)
                .daysUntilDeadline(daysUntilDeadline)
                .build();
    }

    /**
     * 알림 요약 정보 구성
     */
    private CompanyDashboardDto.NotificationSummary buildNotificationSummary(Company company) {
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        User companyUser = company.getUser();
        List<JobPosting> companyJobPostings = jobPostingRepository.findByCompanyUser(companyUser);
        List<JobApplication> allApplications = new ArrayList<>();
        for (JobPosting jobPosting : companyJobPostings) {
            allApplications.addAll(jobApplicationRepository.findByJobPosting(jobPosting));
        }

        // 신규 지원자 알림 (오늘 지원한 사람들)
        Integer newApplicationCount = (int) allApplications.stream()
                .filter(app -> app.getAppliedAt().isAfter(todayStart))
                .count();

        // 마감 임박 공고 (7일 이내 마감)
        LocalDateTime weekLater = LocalDateTime.now().plusDays(7);
        Integer deadlineApproachingCount = (int) companyJobPostings.stream()
                .filter(job -> job.getDeadlineDate() != null &&
                             job.getDeadlineDate().isBefore(weekLater.toLocalDate()) &&
                             job.getDeadlineDate().isAfter(LocalDateTime.now().toLocalDate()))
                .count();

        // 미처리 업무 (서류 검토 대기)
        Integer pendingTaskCount = (int) allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SUBMITTED)
                .count();

        return CompanyDashboardDto.NotificationSummary.builder()
                .newApplicationCount(newApplicationCount)
                .deadlineApproachingCount(deadlineApproachingCount)
                .pendingTaskCount(pendingTaskCount)
                .systemNotificationCount(0) // 추후 시스템 알림 기능 구현 시 추가
                .build();
    }
}