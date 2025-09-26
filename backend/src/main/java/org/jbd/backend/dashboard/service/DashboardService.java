package org.jbd.backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.domain.enums.RequestStatus;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;
import org.jbd.backend.dashboard.dto.CompanyUserDashboardDto;
import org.jbd.backend.dashboard.dto.GeneralUserDashboardDto;
import org.jbd.backend.dashboard.repository.CertificateRequestRepository;
import org.jbd.backend.dashboard.repository.SystemMetricsRepository;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.dto.JobPostingResponseDto;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.repository.JobApplicationRepository;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.user.domain.*;
import org.jbd.backend.user.domain.enums.*;
import org.jbd.backend.user.repository.*;
import org.jbd.backend.user.service.UserService;
import org.jbd.backend.community.repository.PostRepository;
import org.jbd.backend.ai.repository.InterviewRepository;
import org.jbd.backend.ai.domain.InterviewStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final SystemMetricsRepository systemMetricsRepository;
    private final UserService userService;
    private final UserSkillRepository userSkillRepository;
    private final CertificationRepository certificationRepository;
    private final PortfolioRepository portfolioRepository;
    private final EducationRepository educationRepository;
    private final CareerHistoryRepository careerHistoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final InterviewRepository interviewRepository;

    public GeneralUserDashboardDto getGeneralUserDashboard(Long userId) {
        User user = userService.findUserById(userId);
        
        return GeneralUserDashboardDto.builder()
                .totalEmploymentRate(calculateOverallEmploymentRate())
                .myJobScore(calculateJobScore(user))
                .myApplicationStatus(getMyApplicationStatus(user))
                .jobFieldEmployments(getJobFieldEmployments())
                .personalInsight(generatePersonalInsight(user))
                .quickActions(getGeneralUserQuickActions())
                .jobPreparationAnalysis(getJobPreparationAnalysis(user))
                .monthlyProgress(getMonthlyProgress(user))
                .capabilities(getCapabilities(user))
                .build();
    }

    public CompanyUserDashboardDto getCompanyUserDashboard(Long userId) {
        User user = userService.findUserById(userId);
        
        return CompanyUserDashboardDto.builder()
                .totalJobPostings(safeLongToInt(jobPostingRepository.countByCompanyUser(user)))
                .activeJobPostings(safeLongToInt(jobPostingRepository.countByCompanyUserAndStatus(user, JobStatus.PUBLISHED)))
                .totalApplications(safeLongToInt(jobApplicationRepository.countApplicationsByCompanyUser(user)))
                .newApplicationsThisWeek(safeLongToInt(getNewApplicationsThisWeek(user)))
                .myJobPostings(getMyJobPostings(user))
                .popularJobPostings(getPopularJobPostings(user))
                .applicationStatistics(getCompanyApplicationStatistics(user))
                .quickActions(getCompanyQuickActions())
                .build();
    }

    public AdminDashboardDto getAdminDashboard() {
        return AdminDashboardDto.builder()
                .userStatistics(getUserStatistics())
                .newUserStatistics(getNewUserStatistics())
                .jobPostingStatistics(getJobPostingStatistics())
                .applicationStatistics(getApplicationStatisticsForAdmin())
                .certificateRequests(getRecentCertificateRequests())
                .systemStatistics(getSystemStatistics())
                .aiServiceStatistics(getAiServiceStatistics())
                .build();
    }

    // 일반 유저 관련 메서드들
    public Double calculateOverallEmploymentRate() {
        long totalGeneralUsers = userRepository.countActiveUsersByType(UserType.GENERAL);
        if (totalGeneralUsers == 0) return 0.0;
        
        // 성공적으로 취업한 사용자 수 (HIRED 상태의 지원서 수)
        long employedUsers = jobApplicationRepository.countByStatusIn(
            Arrays.asList(ApplicationStatus.HIRED)
        );
        
        return (employedUsers * 100.0) / totalGeneralUsers;
    }

    public Integer calculateJobScore(User user) {
        // 취업 준비도를 다면적으로 평가
        JobPreparationMetrics metrics = calculateDetailedJobPreparationMetrics(user);

        // 가중 평균으로 최종 점수 계산
        double finalScore =
            metrics.profileCompleteness * 0.25 +  // 프로필 완성도 25%
            metrics.skillProficiency * 0.20 +     // 기술 숙련도 20%
            metrics.experienceLevel * 0.20 +      // 경험 수준 20%
            metrics.learningActivity * 0.15 +     // 학습 활동 15%
            metrics.applicationActivity * 0.10 +   // 지원 활동 10%
            metrics.marketReadiness * 0.10;       // 시장 준비도 10%

        return Math.min(Math.max((int)Math.round(finalScore), 25), 100);
    }

    private JobPreparationMetrics calculateDetailedJobPreparationMetrics(User user) {
        JobPreparationMetrics metrics = new JobPreparationMetrics();

        // 1. 프로필 완성도 (0-100점)
        metrics.profileCompleteness = calculateProfileCompleteness(user);

        // 2. 기술 숙련도 (0-100점) - 스킬 레벨과 경험 기반
        metrics.skillProficiency = calculateSkillProficiency(user);

        // 3. 경험 수준 (0-100점) - 경력과 프로젝트 경험
        metrics.experienceLevel = calculateExperienceLevel(user);

        // 4. 학습 활동 (0-100점) - 자격증, 교육 이력
        metrics.learningActivity = calculateLearningActivity(user);

        // 5. 지원 활동 (0-100점) - 지원 이력과 성공률
        metrics.applicationActivity = calculateApplicationActivity(user);

        // 6. 시장 준비도 (0-100점) - 트렌드 기술과 업계 수요
        metrics.marketReadiness = calculateMarketReadiness(user);

        return metrics;
    }

    private int calculateProfileCompleteness(User user) {
        int completeness = 0;

        // UserProfile 조회
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId()).orElse(null);

        // 기본 정보 - 더 엄격한 기준 (기본 정보만으론 부족)
        completeness += user.getName() != null ? 8 : 0;

        if (userProfile != null) {
            completeness += userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().trim().isEmpty() ? 5 : 0;
            completeness += userProfile.getDesiredJob() != null ? 7 : 0;
            completeness += userProfile.getLocation() != null ? 5 : 0; // location이 거주지역
        }

        // 스킬 정보 - 더 높은 기준 요구 (취업을 위한 필수 요소)
        long skillCount = userSkillRepository.countByUserId(user.getId());
        if (skillCount >= 5) completeness += 20;
        else if (skillCount >= 3) completeness += 15;
        else if (skillCount >= 1) completeness += 8;
        // 스킬 없으면 0점

        // 경력 정보 - 실무 경력 중시
        long careerCount = careerHistoryRepository.countByUserId(user.getId());
        if (careerCount >= 2) completeness += 20;
        else if (careerCount >= 1) completeness += 12;
        // 경력 없으면 0점 (신입은 포트폴리오로 대체)

        // 교육 정보 - 세분화된 점수
        long educationCount = educationRepository.countByUserId(user.getId());
        completeness += educationCount > 0 ? 10 : 0;

        // 자격증 정보 - 더 높은 기준
        long certCount = certificationRepository.countByUserId(user.getId());
        if (certCount >= 3) completeness += 15;
        else if (certCount >= 1) completeness += 8;

        // 포트폴리오 정보 - 실제 프로젝트 증명 (신입에게 중요)
        long portfolioCount = portfolioRepository.countByUserId(user.getId());
        if (portfolioCount >= 3) completeness += 20;
        else if (portfolioCount >= 1) completeness += 10;

        return Math.min(completeness, 100);
    }

    @Transactional(readOnly = true)
    private int calculateSkillProficiency(User user) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
        if (userSkills.isEmpty()) return 10; // 스킬 없으면 매우 낮은 점수

        int totalProficiency = 0;
        int skillWeight = 0;

        // 주요 기술 카테고리별 가중치
        Map<SkillCategory, Double> categoryWeights = Map.of(
            SkillCategory.PROGRAMMING_LANGUAGE, 1.0,
            SkillCategory.FRAMEWORK, 0.9,
            SkillCategory.DATABASE, 0.8,
            SkillCategory.CLOUD, 0.7,
            SkillCategory.DEVOPS, 0.6
        );

        for (UserSkill skill : userSkills) {
            try {
                int levelScore = switch (skill.getProficiencyLevel()) {
                    case BEGINNER -> 20;      // 매우 엄격하게 조정
                    case INTERMEDIATE -> 45;  // 초급은 낮은 점수
                    case ADVANCED -> 70;      // 중급도 보통 수준
                    case EXPERT -> 95;        // 고급만 높은 점수
                };

                SkillCategory category = skill.getSkill() != null ?
                    skill.getSkill().getCategory() : SkillCategory.OTHER;
                double weight = categoryWeights.getOrDefault(category, 0.5);
                int experienceBonus = skill.getYearsOfExperience() != null ?
                    Math.min(skill.getYearsOfExperience() * 3, 15) : 0;

                totalProficiency += (int)((levelScore + experienceBonus) * weight);
                skillWeight += weight;
            } catch (Exception e) {
                logger.warn("SkillMaster 접근 실패: {}", e.getMessage());
                int levelScore = switch (skill.getProficiencyLevel()) {
                    case BEGINNER -> 20;      // 매우 엄격하게 조정
                    case INTERMEDIATE -> 45;  // 초급은 낮은 점수
                    case ADVANCED -> 70;      // 중급도 보통 수준
                    case EXPERT -> 95;        // 고급만 높은 점수
                };
                totalProficiency += levelScore;
                skillWeight += 0.5; // 기본 가중치
            }
        }

        return skillWeight > 0 ? (int)(totalProficiency / skillWeight) : 30;
    }

    private int calculateExperienceLevel(User user) {
        int experienceScore = 15; // 더 엄격한 기본 점수

        // 경력 기간 계산
        List<CareerHistory> careers = careerHistoryRepository.findByUserIdOrderByStartDateDesc(user.getId());
        int totalMonths = 0;
        for (CareerHistory career : careers) {
            LocalDate endDate = career.getEndDate() != null ? career.getEndDate() : LocalDate.now();
            totalMonths += java.time.temporal.ChronoUnit.MONTHS.between(career.getStartDate(), endDate);
        }
        experienceScore += Math.min(totalMonths * 2, 40); // 최대 40점

        // 프로젝트 경험
        long portfolioCount = portfolioRepository.countByUserId(user.getId());
        experienceScore += Math.min(portfolioCount * 5, 20); // 최대 20점

        return Math.min(experienceScore, 100);
    }

    private int calculateLearningActivity(User user) {
        int learningScore = 10; // 더 엄격한 기본 점수

        // 최신 자격증 (최근 2년)
        List<Certification> recentCerts = certificationRepository.findByUserIdOrderByIssueDateDesc(user.getId())
            .stream()
            .filter(cert -> cert.getIssueDate().isAfter(LocalDate.now().minusYears(2)))
            .collect(Collectors.toList());
        learningScore += Math.min(recentCerts.size() * 10, 40);

        // 교육 수준
        List<Education> educations = educationRepository.findByUserIdOrderByGraduationYearDesc(user.getId());
        if (!educations.isEmpty()) {
            Education highestEducation = educations.get(0);
            learningScore += switch (highestEducation.getEducationLevel()) {
                case DOCTORATE -> 30;
                case MASTER -> 25;
                case BACHELOR -> 20;
                case ASSOCIATE -> 15;
                default -> 10;
            };
        }

        // 지속적인 학습 (스킬 추가 빈도)
        long daysSinceJoined = java.time.temporal.ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
        long skillCount = userSkillRepository.countByUserId(user.getId());
        if (daysSinceJoined > 0) {
            double learningRate = skillCount / (daysSinceJoined / 30.0); // 월평균 스킬 추가
            learningScore += Math.min((int)(learningRate * 10), 10);
        }

        return Math.min(learningScore, 100);
    }

    private int calculateApplicationActivity(User user) {
        long applicationCount = jobApplicationRepository.countByUser(user);
        long interviewCount = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW_SCHEDULED);
        long hiredCount = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.HIRED);

        if (applicationCount == 0) return 5; // 지원 활동 없으면 매우 낮은 점수

        int activityScore = 10; // 지원 활동을 시작해야 점수 상승

        // 지원 활동 점수
        activityScore += Math.min(applicationCount * 3, 30);

        // 면접 성공률
        if (applicationCount > 0) {
            double interviewRate = (double)interviewCount / applicationCount;
            activityScore += (int)(interviewRate * 25);
        }

        // 채용 성공률
        if (interviewCount > 0) {
            double hireRate = (double)hiredCount / interviewCount;
            activityScore += (int)(hireRate * 25);
        }

        return Math.min(activityScore, 100);
    }

    private int calculateMarketReadiness(User user) {
        int readinessScore = 25; // 더 엄격한 기본 점수

        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());

        // 인기 기술 스택 보유 여부 체크
        Set<String> trendingSkills = Set.of(
            "React", "Vue.js", "Node.js", "Python", "Java", "Spring Boot",
            "AWS", "Docker", "Kubernetes", "MongoDB", "PostgreSQL"
        );

        long trendingSkillCount = userSkills.stream()
            .filter(skill -> trendingSkills.contains(skill.getSkill().getSkillName()))
            .count();
        readinessScore += Math.min(trendingSkillCount * 5, 30);

        // 풀스택 개발자 점수 (프론트엔드 + 백엔드)
        boolean hasFrontend = userSkills.stream()
            .anyMatch(skill -> skill.getSkill().getCategory() == SkillCategory.FRONTEND);
        boolean hasBackend = userSkills.stream()
            .anyMatch(skill -> skill.getSkill().getCategory() == SkillCategory.BACKEND);
        if (hasFrontend && hasBackend) {
            readinessScore += 15;
        }

        // 클라우드/DevOps 기술
        boolean hasCloudSkill = userSkills.stream()
            .anyMatch(skill -> skill.getSkill().getCategory() == SkillCategory.CLOUD ||
                              skill.getSkill().getCategory() == SkillCategory.DEVOPS);
        if (hasCloudSkill) {
            readinessScore += 5;
        }

        return Math.min(readinessScore, 100);
    }

    // 내부 클래스: 취업 준비도 지표들
    private static class JobPreparationMetrics {
        int profileCompleteness = 0;
        int skillProficiency = 0;
        int experienceLevel = 0;
        int learningActivity = 0;
        int applicationActivity = 0;
        int marketReadiness = 0;
    }

    private GeneralUserDashboardDto.MyApplicationStatusDto getMyApplicationStatus(User user) {
        // 직접 카운트 방식으로 변경 - 타입 변환 문제 해결
        System.out.println("=== DIRECT COUNT DEBUG: Application Statistics for User " + user.getId() + " ===");

        long totalApplications = jobApplicationRepository.countByUser(user);
        long pendingApplications = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.SUBMITTED) +
                                  jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.REVIEWED);
        long interviewApplications = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.DOCUMENT_PASSED) +
                                    jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW_SCHEDULED) +
                                    jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW_PASSED);
        long rejectedApplications = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.REJECTED);
        long acceptedApplications = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.HIRED);

        System.out.println("Direct count results: total=" + totalApplications + ", pending=" + pendingApplications +
                          ", interview=" + interviewApplications + ", rejected=" + rejectedApplications + ", accepted=" + acceptedApplications);
        System.out.println("=== END DIRECT COUNT DEBUG ===");
        
        return GeneralUserDashboardDto.MyApplicationStatusDto.builder()
                .totalApplications(Math.toIntExact(totalApplications))
                .pendingApplications(Math.toIntExact(pendingApplications))
                .interviewApplications(Math.toIntExact(interviewApplications))
                .rejectedApplications(Math.toIntExact(rejectedApplications))
                .acceptedApplications(Math.toIntExact(acceptedApplications))
                .build();
    }

    private List<GeneralUserDashboardDto.JobFieldEmploymentDto> getJobFieldEmployments() {
        List<Object[]> statisticsData = jobApplicationRepository.findJobFieldEmploymentStatistics();
        List<GeneralUserDashboardDto.JobFieldEmploymentDto> jobFieldList = new ArrayList<>();

        for (Object[] data : statisticsData) {
            String jobField = (String) data[0];
            Integer totalApplicants = ((Number) data[1]).intValue();
            Integer employedCount = ((Number) data[2]).intValue();

            double employmentRate = totalApplicants > 0 ? (employedCount * 100.0) / totalApplicants : 0.0;

            jobFieldList.add(GeneralUserDashboardDto.JobFieldEmploymentDto.builder()
                    .jobField(jobField)
                    .employmentRate(Math.round(employmentRate * 10) / 10.0) // 소수점 첫째자리까지
                    .totalApplicants(totalApplicants)
                    .employedCount(employedCount)
                    .build());
        }

        // 데이터가 없는 경우 기본값 반환
        if (jobFieldList.isEmpty()) {
            jobFieldList.add(GeneralUserDashboardDto.JobFieldEmploymentDto.builder()
                    .jobField("전체")
                    .employmentRate(0.0)
                    .totalApplicants(0)
                    .employedCount(0)
                    .build());
        }

        return jobFieldList;
    }

    public GeneralUserDashboardDto.PersonalInsightDto generatePersonalInsight(User user) {
        // 사용자 데이터 기반으로 개인화된 인사이트 생성
        long applicationCount = jobApplicationRepository.countByUser(user);
        long interviewCount = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW_SCHEDULED);
        long hiredCount = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.HIRED);
        long rejectedCount = jobApplicationRepository.countByUserAndStatus(user, ApplicationStatus.REJECTED);

        List<String> recommendations = new ArrayList<>();
        List<String> skillsToImprove = new ArrayList<>();
        List<String> suggestedActions = new ArrayList<>();
        String overallFeedback;

        // 지원서 기반 추천
        if (applicationCount < 5) {
            recommendations.add("더 많은 채용공고에 지원해보세요 (현재 " + applicationCount + "개 지원)");
            suggestedActions.add("이번 주에 최소 3개 이상의 채용공고에 지원하기");
        } else {
            recommendations.add("꾸준한 지원 활동을 하고 계시네요!");
        }

        // 면접 경험 기반 추천
        if (interviewCount == 0 && applicationCount > 0) {
            skillsToImprove.add("서류 작성 능력");
            suggestedActions.add("이력서와 자기소개서 피드백 받기");
        } else if (interviewCount > 0) {
            recommendations.add("면접 경험이 있으시니 자신감을 가지세요!");
            if (hiredCount == 0) {
                skillsToImprove.add("면접 대답 스킬");
                suggestedActions.add("모의 면접 연습하기");
            }
        }

        // 성공률 기반 피드백
        if (hiredCount > 0) {
            overallFeedback = "축하합니다! 이미 " + hiredCount + "개의 합격 경험이 있으시네요. 이 경험을 바탕으로 더 좋은 기회를 찾아보세요!";
            recommendations.add("현재 성과를 바탕으로 더 도전적인 포지션을 고려해보세요");
        } else if (applicationCount > 10) {
            overallFeedback = "많은 지원을 하고 계시는군요. 결과가 나오지 않는다면 전략을 점검해보는 것이 좋겠습니다.";
            suggestedActions.add("지원 전략 점검 및 멘토링 받기");
        } else {
            overallFeedback = "취업 준비를 시작하고 계시는군요! 꾸준한 준비와 지원이 중요합니다.";
        }

        // 기본 스킬 개선 영역
        if (skillsToImprove.isEmpty()) {
            skillsToImprove.addAll(Arrays.asList(
                "기술 스택 심화",
                "프로젝트 경험",
                "커뮤니케이션 스킬"
            ));
        }

        // 기본 추천 액션
        if (suggestedActions.isEmpty()) {
            suggestedActions.addAll(Arrays.asList(
                "GitHub 프로필 업데이트하기",
                "네트워킹 활동 참여하기",
                "업계 트렌드 학습하기"
            ));
        }

        return GeneralUserDashboardDto.PersonalInsightDto.builder()
                .recommendations(recommendations)
                .skillsToImprove(skillsToImprove)
                .suggestedActions(suggestedActions)
                .overallFeedback(overallFeedback)
                .build();
    }

    private GeneralUserDashboardDto.QuickActionsDto getGeneralUserQuickActions() {
        return GeneralUserDashboardDto.QuickActionsDto.builder()
                .mockInterviewUrl("/ai/interview")
                .customJobPostingsUrl("/job-postings/recommended")
                .profileUpdateUrl("/profile/edit")
                .resumeGeneratorUrl("/resume/generator")
                .build();
    }

    // 기업 유저 관련 메서드들
    private Long getNewApplicationsThisWeek(User companyUser) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return jobApplicationRepository.countApplicationsByCompanyUserAndDateAfter(companyUser, oneWeekAgo);
    }

    private List<JobPostingResponseDto> getMyJobPostings(User companyUser) {
        // N+1 문제 해결: 채용공고와 지원자 수를 한 번에 조회하여 성능 최적화
        List<JobPostingResponseDto> result = jobPostingRepository
                .findByCompanyUserWithApplicationCountOrderByCreatedAtDesc(
                        companyUser, PageRequest.of(0, 5))
                .stream()
                .map(row -> {
                    JobPosting jobPosting = (JobPosting) row[0];
                    Long applicationCount = (Long) row[1];
                    
                    JobPostingResponseDto dto = JobPostingResponseDto.from(jobPosting);
                    // 추가적으로 지원자 수 정보도 설정 가능
                    return dto;
                })
                .collect(Collectors.toList());
        
        return result;
    }

    private List<CompanyUserDashboardDto.PopularJobPostingDto> getPopularJobPostings(User companyUser) {
        // N+1 문제 해결: 채용공고와 지원자 수를 한 번에 조회
        List<CompanyUserDashboardDto.PopularJobPostingDto> result = jobPostingRepository
                .findByCompanyUserWithApplicationCountOrderByViewCountDesc(
                        companyUser, PageRequest.of(0, 5))
                .stream()
                .map(row -> {
                    JobPosting jobPosting = (JobPosting) row[0];
                    Long applicationCount = (Long) row[1];
                    
                    return CompanyUserDashboardDto.PopularJobPostingDto.builder()
                            .jobPostingId(jobPosting.getId())
                            .title(jobPosting.getTitle())
                            .companyName(jobPosting.getCompanyName())
                            .applicationCount(Math.toIntExact(applicationCount))
                            .viewCount(Math.toIntExact(jobPosting.getViewCount()))
                            .status(jobPosting.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
        
        return result;
    }

    private CompanyUserDashboardDto.ApplicationStatisticsDto getCompanyApplicationStatistics(User companyUser) {
        // 성능 최적화: 한 번의 쿼리로 모든 상태별 통계 조회
        Object[] statistics = jobApplicationRepository.findApplicationStatisticsByCompanyUser(companyUser);
        
        Integer pending = safeArrayAccess(statistics, 0, 0);
        Integer documentPassed = safeArrayAccess(statistics, 1, 0);
        Integer interviewScheduled = safeArrayAccess(statistics, 2, 0);
        Integer finalPassed = safeArrayAccess(statistics, 3, 0);
        Integer rejected = safeArrayAccess(statistics, 4, 0);
        Integer totalApplications = safeArrayAccess(statistics, 5, 0);
        
        Long totalJobPostings = jobPostingRepository.countByCompanyUser(companyUser);
        Double averageApplicationsPerPosting = totalJobPostings > 0 ? totalApplications.doubleValue() / totalJobPostings : 0.0;
        
        return CompanyUserDashboardDto.ApplicationStatisticsDto.builder()
                .pendingReview(pending)
                .documentPassed(documentPassed)
                .interviewScheduled(interviewScheduled)
                .finalPassed(finalPassed)
                .rejected(rejected)
                .averageApplicationsPerPosting(averageApplicationsPerPosting)
                .build();
    }

    private CompanyUserDashboardDto.CompanyQuickActionsDto getCompanyQuickActions() {
        return CompanyUserDashboardDto.CompanyQuickActionsDto.builder()
                .createJobPostingUrl("/job-postings/create")
                .manageApplicationsUrl("/applications/manage")
                .companyProfileUrl("/company/profile")
                .statisticsUrl("/company/statistics")
                .build();
    }

    // 관리자 관련 메서드들
    private AdminDashboardDto.UserStatisticsDto getUserStatistics() {
        // 성능 최적화: 한 번의 쿼리로 모든 사용자 통계 조회
        Object[] statistics = userRepository.findUserStatistics();
        
        Long generalUsers = safeArrayAccessLong(statistics, 0, 0L);
        Long companyUsers = safeArrayAccessLong(statistics, 1, 0L);
        Long adminUsers = safeArrayAccessLong(statistics, 2, 0L);
        Long totalUsers = safeArrayAccessLong(statistics, 3, 0L);
        
        Long activeUsers = generalUsers + companyUsers + adminUsers;
        Long inactiveUsers = totalUsers - activeUsers;
        
        return AdminDashboardDto.UserStatisticsDto.builder()
                .totalUsers(totalUsers)
                .generalUsers(generalUsers)
                .companyUsers(companyUsers)
                .adminUsers(adminUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .build();
    }

    private AdminDashboardDto.NewUserStatisticsDto getNewUserStatistics() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekAgo = today.minusWeeks(1);
        LocalDateTime monthAgo = today.minusMonths(1);
        
        // 성능 최적화: 한 번의 쿼리로 모든 신규 사용자 통계 조회
        Object[] statistics = userRepository.findNewUserStatistics(today, weekAgo, monthAgo);
        
        Long todayNewUsers = safeArrayAccessLong(statistics, 0, 0L);
        Long thisWeekNewUsers = safeArrayAccessLong(statistics, 1, 0L);
        Long thisMonthNewUsers = safeArrayAccessLong(statistics, 2, 0L);
        Long todayGeneralUsers = safeArrayAccessLong(statistics, 3, 0L);
        Long todayCompanyUsers = safeArrayAccessLong(statistics, 4, 0L);
        
        return AdminDashboardDto.NewUserStatisticsDto.builder()
                .todayNewUsers(todayNewUsers)
                .thisWeekNewUsers(thisWeekNewUsers)
                .thisMonthNewUsers(thisMonthNewUsers)
                .todayGeneralUsers(todayGeneralUsers)
                .todayCompanyUsers(todayCompanyUsers)
                .build();
    }

    private AdminDashboardDto.JobPostingStatisticsDto getJobPostingStatistics() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        // 성능 최적화: 한 번의 쿼리로 채용공고 통계 조회
        Object[] statistics = jobPostingRepository.findJobPostingStatistics(weekAgo);
        
        Long totalJobPostings = safeArrayAccessLong(statistics, 0, 0L);
        Long activeJobPostings = safeArrayAccessLong(statistics, 1, 0L);
        Long closedJobPostings = safeArrayAccessLong(statistics, 2, 0L);
        Long thisWeekJobPostings = safeArrayAccessLong(statistics, 3, 0L);
        
        Long totalApplications = jobApplicationRepository.count();
        Double averageApplicationsPerPosting = totalJobPostings > 0 ? totalApplications.doubleValue() / totalJobPostings : 0.0;
        
        return AdminDashboardDto.JobPostingStatisticsDto.builder()
                .totalJobPostings(totalJobPostings)
                .activeJobPostings(activeJobPostings)
                .closedJobPostings(closedJobPostings)
                .thisWeekJobPostings(thisWeekJobPostings)
                .averageApplicationsPerPosting(averageApplicationsPerPosting)
                .build();
    }

    private AdminDashboardDto.ApplicationStatisticsDto getApplicationStatisticsForAdmin() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        // 성능 최적화: 한 번의 쿼리로 지원서 통계 조회
        Object[] statistics = jobApplicationRepository.findAdminApplicationStatistics(weekAgo);
        
        Long totalApplications = safeArrayAccessLong(statistics, 0, 0L);
        Long thisWeekApplications = safeArrayAccessLong(statistics, 1, 0L);
        Long pendingApplications = safeArrayAccessLong(statistics, 2, 0L);
        Long successfulApplications = safeArrayAccessLong(statistics, 3, 0L);
        
        Double successRate = totalApplications > 0 ? (successfulApplications * 100.0) / totalApplications : 0.0;
        
        return AdminDashboardDto.ApplicationStatisticsDto.builder()
                .totalApplications(totalApplications)
                .thisWeekApplications(thisWeekApplications)
                .pendingApplications(pendingApplications)
                .successfulApplications(successfulApplications)
                .successRate(successRate)
                .build();
    }

    private List<AdminDashboardDto.CertificateRequestDto> getRecentCertificateRequests() {
        List<CertificateRequest> requests = certificateRequestRepository
                .findRecentPendingRequests(PageRequest.of(0, 10));
        
        return requests.stream()
                .map(request -> AdminDashboardDto.CertificateRequestDto.builder()
                        .requestId(request.getId())
                        .userEmail(request.getUser().getEmail())
                        .userName(request.getUser().getName())
                        .certificateType(request.getCertificateType().getDescription())
                        .requestedAt(request.getCreatedAt())
                        .status(request.getStatus().getDescription())
                        .purpose(request.getPurpose())
                        .build())
                .collect(Collectors.toList());
    }

    private AdminDashboardDto.SystemStatisticsDto getSystemStatistics() {
        Long totalApiCalls = systemMetricsRepository.findTotalApiCalls();
        Long dailyActiveUsers = systemMetricsRepository.findTodayActiveUsers().orElse(0L);
        Double systemUptime = 99.5; // 임시 하드코딩 (실제로는 createdAt 기반 계산 필요)
        Long errorCount = systemMetricsRepository.findTodayErrorCount().orElse(0L);
        
        // 오늘 자격증 요청 수 추가 (수정된 메서드 파라미터 반영)
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        Long todayCertificateRequests = certificateRequestRepository.countTodayRequests(todayStart, todayEnd);
        
        return AdminDashboardDto.SystemStatisticsDto.builder()
                .totalApiCalls(totalApiCalls)
                .dailyActiveUsers(dailyActiveUsers)
                .systemUptime(systemUptime)
                .errorCount(safeLongToInt(errorCount))
                .build();
    }
    
    private int safeLongToInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            long longValue = ((Number) value).longValue();
            // MySQL COUNT()는 BIGINT를 반환하므로 long에서 int로 안전하게 변환
            return Math.toIntExact(longValue);
        }
        // 디버그 로깅 추가
        System.out.println("UNEXPECTED TYPE in safeLongToInt: " + value.getClass().getSimpleName() + " = " + value);
        return 0;
    }
    
    private Long safeLongFromObject(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
    
    private int safeArrayAccess(Object[] array, int index, int defaultValue) {
        if (array == null || index >= array.length) {
            return defaultValue;
        }
        return safeLongToInt(array[index]);
    }
    
    private Long safeArrayAccessLong(Object[] array, int index, Long defaultValue) {
        if (array == null || index >= array.length) {
            return defaultValue;
        }
        return safeLongFromObject(array[index]);
    }
    
    // 새로 추가된 메서드들
    private GeneralUserDashboardDto.JobPreparationAnalysisDto getJobPreparationAnalysis(User user) {
        int myScore = calculateJobScore(user);
        int averageScore = calculateAverageJobScore();
        int targetScore = 80; // 목표 점수 (실제로는 설정값으로 관리)

        return GeneralUserDashboardDto.JobPreparationAnalysisDto.builder()
                .myScore(myScore)
                .averageScore(averageScore)
                .targetScore(targetScore)
                .build();
    }

    /**
     * 전체 일반 사용자들의 평균 취업 점수 계산
     */
    private int calculateAverageJobScore() {
        List<User> generalUsers = userRepository.findAllGeneralUsers();
        if (generalUsers.isEmpty()) {
            return 50; // 기본값
        }

        int totalScore = 0;
        for (User user : generalUsers) {
            totalScore += calculateJobScore(user);
        }

        return totalScore / generalUsers.size();
    }
    
    private List<GeneralUserDashboardDto.MonthlyProgressDto> getMonthlyProgress(User user) {
        // 최근 6개월 데이터 조회
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> monthlyData = jobApplicationRepository.findMonthlyProgressByUser(user, sixMonthsAgo);

        List<GeneralUserDashboardDto.MonthlyProgressDto> progressList = new ArrayList<>();

        // 월 이름 배열
        String[] monthNames = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};

        for (Object[] data : monthlyData) {
            int year = ((Number) data[0]).intValue();
            int month = ((Number) data[1]).intValue();
            int applications = ((Number) data[2]).intValue();
            int interviews = ((Number) data[3]).intValue();
            int offers = ((Number) data[4]).intValue();

            String monthLabel = monthNames[month - 1]; // 배열은 0부터 시작

            progressList.add(GeneralUserDashboardDto.MonthlyProgressDto.builder()
                    .month(monthLabel)
                    .applications(applications)
                    .interviews(interviews)
                    .offers(offers)
                    .build());
        }

        // 데이터가 없는 경우 기본값 반환
        if (progressList.isEmpty()) {
            progressList.add(GeneralUserDashboardDto.MonthlyProgressDto.builder()
                    .month("이번달")
                    .applications(0)
                    .interviews(0)
                    .offers(0)
                    .build());
        }

        return progressList;
    }
    
    @Transactional(readOnly = true)
    private List<GeneralUserDashboardDto.CapabilityDto> getCapabilities(User user) {
        List<GeneralUserDashboardDto.CapabilityDto> capabilities = new ArrayList<>();

        // userSkills를 전체 메서드 범위에서 사용할 수 있도록 선언
        List<UserSkill> userSkills = new ArrayList<>();

        try {
            // 1. 기술 스킬 점수 계산 - 스킬 레벨과 카테고리별 가중치 적용
            int technicalSkillScore = 50; // 기본값
            try {
                userSkills = userSkillRepository.findByUserId(user.getId());
                technicalSkillScore = calculateTechnicalSkillScore(userSkills);
            } catch (Exception e) {
                logger.warn("기술 스킬 점수 계산 실패, 기본값 사용: {}", e.getMessage());
            }

            capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("기술 스킬")
                    .level(technicalSkillScore)
                    .build());

        // 2. 프로젝트 경험 점수 - 포트폴리오와 경력 기반
        int portfolioCount = (int) portfolioRepository.countByUserId(user.getId());
        List<CareerHistory> careerHistories = careerHistoryRepository.findByUserIdOrderByStartDateDesc(user.getId());
        int projectExperienceScore = calculateProjectExperienceScore(portfolioCount, careerHistories);

        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("프로젝트 경험")
                .level(projectExperienceScore)
                .build());

        // 3. 학습 능력 점수 - 자격증과 교육 이력 기반
        List<Certification> certifications = certificationRepository.findByUserIdOrderByIssueDateDesc(user.getId());
        List<Education> educations = educationRepository.findByUserIdOrderByGraduationYearDesc(user.getId());
        int learningAbilityScore = calculateLearningAbilityScore(certifications, educations);

        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("학습 능력")
                .level(learningAbilityScore)
                .build());

        // 4. 취업 준비도 점수 - 종합적인 프로필 완성도
        int jobReadinessScore = calculateJobReadinessScore(userSkills, portfolioCount,
                certifications.size(), educations.size(), careerHistories);

        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("취업 준비도")
                .level(jobReadinessScore)
                .build());

        // 5. 지원 활동 점수 - 고정값 처리 (데이터 연동 이슈로 인해)
        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("지원 활동")
                .level(10) // 고정값
                .build());

        // 6. 커뮤니케이션 점수 - 고정값 처리 (데이터 연동 이슈로 인해)
        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("커뮤니케이션")
                .level(25) // 고정값
                .build());

        // 7. 활동 지속성 점수 - 고정값 처리 (데이터 연동 이슈로 인해)
        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("활동 지속성")
                .level(0) // 고정값
                .build());

        // 8. 전문성 점수 - 고급 기술과 경력 기반
        int expertiseScore = calculateExpertiseScore(userSkills, careerHistories, certifications);

        capabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                .skill("전문성")
                .level(expertiseScore)
                .build());

            return capabilities;
        } catch (Exception e) {
            logger.error("대시보드 역량 계산 중 오류 발생: {}", e.getMessage(), e);

            // 기본 역량 데이터 반환
            List<GeneralUserDashboardDto.CapabilityDto> defaultCapabilities = new ArrayList<>();
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("기술 스킬")
                    .level(40)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("프로젝트 경험")
                    .level(35)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("학습 능력")
                    .level(45)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("취업 준비도")
                    .level(50)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("지원 활동")
                    .level(30)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("커뮤니케이션")
                    .level(40)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("활동 지속성")
                    .level(35)
                    .build());
            defaultCapabilities.add(GeneralUserDashboardDto.CapabilityDto.builder()
                    .skill("전문성")
                    .level(30)
                    .build());

            return defaultCapabilities;
        }
    }

    // 기술 스킬 점수 계산 메서드
    private int calculateTechnicalSkillScore(List<UserSkill> userSkills) {
        if (userSkills.isEmpty()) return 20;

        int totalScore = 0;
        Map<SkillCategory, Integer> categoryWeights = Map.of(
            SkillCategory.PROGRAMMING_LANGUAGE, 100,
            SkillCategory.FRAMEWORK, 90,
            SkillCategory.BACKEND, 85,
            SkillCategory.FRONTEND, 85,
            SkillCategory.DATABASE, 80,
            SkillCategory.CLOUD, 75,
            SkillCategory.DEVOPS, 75,
            SkillCategory.AI_ML, 70,
            SkillCategory.MOBILE, 65
        );

        for (UserSkill userSkill : userSkills) {
            try {
                int skillLevelScore = getSkillLevelScore(userSkill.getProficiencyLevel());
                // SkillMaster 안전하게 접근
                SkillCategory category = userSkill.getSkill() != null ?
                    userSkill.getSkill().getCategory() : SkillCategory.OTHER;
                int categoryWeight = categoryWeights.getOrDefault(category, 50);
                int experienceBonus = userSkill.getYearsOfExperience() != null ?
                    Math.min(userSkill.getYearsOfExperience() * 5, 20) : 0;

                totalScore += (skillLevelScore * categoryWeight / 100) + experienceBonus;
            } catch (Exception e) {
                // LazyInitializationException 등의 문제가 발생하면 기본값 사용
                logger.warn("SkillMaster에 접근할 수 없습니다: {}", e.getMessage());
                int skillLevelScore = getSkillLevelScore(userSkill.getProficiencyLevel());
                totalScore += skillLevelScore; // 기본 스킬 점수만 적용
            }
        }

        int averageScore = totalScore / Math.max(userSkills.size(), 1);
        return Math.min(Math.max(averageScore, 20), 100);
    }

    // 프로젝트 경험 점수 계산
    private int calculateProjectExperienceScore(int portfolioCount, List<CareerHistory> careerHistories) {
        int baseScore = 25;
        int portfolioScore = Math.min(portfolioCount * 15, 40);

        int careerScore = 0;
        for (CareerHistory career : careerHistories) {
            long months = java.time.temporal.ChronoUnit.MONTHS.between(
                career.getStartDate(), career.getEndDate() != null ? career.getEndDate() : LocalDate.now());
            careerScore += Math.min(months * 2, 25);
        }
        careerScore = Math.min(careerScore, 35);

        return Math.min(baseScore + portfolioScore + careerScore, 100);
    }

    // 학습 능력 점수 계산
    private int calculateLearningAbilityScore(List<Certification> certifications, List<Education> educations) {
        int baseScore = 30;

        int certScore = 0;
        for (Certification cert : certifications) {
            if (cert.getIsActive()) {
                certScore += cert.getExpiryDate() != null && cert.getExpiryDate().isAfter(LocalDate.now()) ? 15 : 10;
            }
        }
        certScore = Math.min(certScore, 40);

        int eduScore = 0;
        for (Education edu : educations) {
            switch (edu.getEducationLevel()) {
                case DOCTORATE -> eduScore += 25;
                case MASTER -> eduScore += 20;
                case BACHELOR -> eduScore += 15;
                case ASSOCIATE -> eduScore += 10;
                case HIGH_SCHOOL -> eduScore += 5;
                default -> eduScore += 3;
            }
        }
        eduScore = Math.min(eduScore, 30);

        return Math.min(baseScore + certScore + eduScore, 100);
    }

    // 취업 준비도 점수 계산
    private int calculateJobReadinessScore(List<UserSkill> skills, int portfolioCount,
                                         int certificationCount, int educationCount,
                                         List<CareerHistory> careerHistories) {
        int profileCompleteness = 0;
        profileCompleteness += skills.isEmpty() ? 0 : 25;
        profileCompleteness += portfolioCount > 0 ? 20 : 0;
        profileCompleteness += certificationCount > 0 ? 15 : 0;
        profileCompleteness += educationCount > 0 ? 15 : 0;
        profileCompleteness += !careerHistories.isEmpty() ? 25 : 0;

        return Math.max(profileCompleteness, 25);
    }

    // 지원 활동 점수 계산
    private int calculateApplicationActivityScore(long applicationCount, long interviewCount, long hiredCount) {
        int baseScore = applicationCount > 0 ? 30 : 10;
        int interviewBonus = Math.min((int)(interviewCount * 15), 35);
        int successBonus = Math.min((int)(hiredCount * 25), 35);

        return Math.min(baseScore + interviewBonus + successBonus, 100);
    }

    // 커뮤니케이션 점수 계산
    private int calculateCommunicationScore(long interviewCount, long applicationCount,
                                          long hiredCount, long communicationSkillCount) {
        int baseScore = 25;

        // 면접 성공률 기반
        double interviewSuccessRate = applicationCount > 0 ? (double)interviewCount / applicationCount : 0;
        int interviewScore = (int)(interviewSuccessRate * 30);

        // 채용 성공률 기반
        double hireSuccessRate = interviewCount > 0 ? (double)hiredCount / interviewCount : 0;
        int hireScore = (int)(hireSuccessRate * 25);

        // 커뮤니케이션 관련 스킬 보너스
        int skillBonus = (int)(communicationSkillCount * 10);

        return Math.min(baseScore + interviewScore + hireScore + skillBonus, 100);
    }

    // 지속성 점수 계산
    private int calculateConsistencyScore(long daysSinceJoined, long applicationCount, int skillCount) {
        int durationScore = Math.min((int)(daysSinceJoined / 30), 40); // 월 단위

        // 활동 빈도 점수
        double activityRate = daysSinceJoined > 0 ? (double)(applicationCount + skillCount) / (daysSinceJoined / 30.0) : 0;
        int activityScore = Math.min((int)(activityRate * 20), 60);

        return Math.min(durationScore + activityScore, 100);
    }

    // 전문성 점수 계산
    private int calculateExpertiseScore(List<UserSkill> skills, List<CareerHistory> careers,
                                      List<Certification> certifications) {
        int baseScore = 20;

        // 고급 스킬 개수
        long advancedSkillCount = skills.stream()
                .filter(skill -> skill.getProficiencyLevel() == SkillLevel.ADVANCED ||
                               skill.getProficiencyLevel() == SkillLevel.EXPERT)
                .count();
        int advancedScore = Math.min((int)(advancedSkillCount * 15), 40);

        // 경력 기간
        int totalCareerMonths = careers.stream()
                .mapToInt(career -> (int)java.time.temporal.ChronoUnit.MONTHS.between(
                    career.getStartDate(), career.getEndDate() != null ? career.getEndDate() : LocalDate.now()))
                .sum();
        int careerScore = Math.min(totalCareerMonths * 2, 25);

        // 전문 자격증
        long professionalCertCount = certifications.stream()
                .filter(cert -> cert.getIsActive())
                .count();
        int certScore = Math.min((int)(professionalCertCount * 5), 15);

        return Math.min(baseScore + advancedScore + careerScore + certScore, 100);
    }

    // 스킬 레벨에 따른 점수 반환
    private int getSkillLevelScore(SkillLevel level) {
        return switch (level) {
            case BEGINNER -> 40;
            case INTERMEDIATE -> 60;
            case ADVANCED -> 80;
            case EXPERT -> 100;
        };
    }

    /**
     * AI 서비스 통계를 조회합니다.
     * 필요한 AI 서비스(면접, 이미지 생성, 감정분석)만 실제 데이터로 제공합니다.
     */
    private AdminDashboardDto.AiServiceStatisticsDto getAiServiceStatistics() {
        try {
            // 실제 AI 면접 통계 (Interview 테이블에서 조회)
            Long totalInterviews = interviewRepository.countTotalInterviews();
            Long completedInterviews = interviewRepository.countInterviewsByStatus(InterviewStatus.COMPLETED);
            Double averageScore = interviewRepository.findAverageScoreByStatus(InterviewStatus.COMPLETED);

            // 실제 감정분석 통계 (Posts 테이블에서 조회)
            Long totalSentimentAnalyses = postRepository.countTotalSentimentAnalyses();
            Long positivePosts = postRepository.countPositivePosts();
            Long negativePosts = postRepository.countNegativePosts();

            // 긍정도율 계산
            double positivityRate = totalSentimentAnalyses > 0 ?
                (positivePosts.doubleValue() / totalSentimentAnalyses.doubleValue()) * 100.0 : 0.0;

            // 실제 이미지 생성 통계 (Posts 테이블에서 조회)
            Long totalImageGenerations = postRepository.countPostsWithImages();

            return AdminDashboardDto.AiServiceStatisticsDto.builder()
                    // AI 면접 통계 (실제 데이터)
                    .totalInterviews(totalInterviews)
                    .completedInterviews(completedInterviews)
                    .averageInterviewScore(averageScore != null ? Math.round(averageScore * 10.0) / 10.0 : 0.0)

                    // 이미지 생성 통계 (실제 데이터)
                    .totalImageGenerations(totalImageGenerations)

                    // 감정분석 통계 (실제 데이터)
                    .totalSentimentAnalyses(totalSentimentAnalyses)
                    .positivePosts(positivePosts)
                    .negativePosts(negativePosts)
                    .positivityRate(Math.round(positivityRate * 10.0) / 10.0) // 소수점 1자리
                    .build();

        } catch (Exception e) {
            logger.error("AI 서비스 통계 조회 중 오류 발생", e);
            // 오류 발생 시 기본값 반환
            return AdminDashboardDto.AiServiceStatisticsDto.builder()
                    .totalInterviews(0L)
                    .completedInterviews(0L)
                    .averageInterviewScore(0.0)
                    .totalImageGenerations(0L)
                    .totalSentimentAnalyses(0L)
                    .positivePosts(0L)
                    .negativePosts(0L)
                    .positivityRate(0.0)
                    .build();
        }
    }
}