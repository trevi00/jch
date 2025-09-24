package org.jbd.backend.dashboard.service;

import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.domain.CertificateType;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;
import org.jbd.backend.dashboard.dto.CompanyUserDashboardDto;
import org.jbd.backend.dashboard.dto.GeneralUserDashboardDto;
import org.jbd.backend.dashboard.repository.CertificateRequestRepository;
import org.jbd.backend.dashboard.repository.SystemMetricsRepository;
import org.jbd.backend.job.repository.JobApplicationRepository;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 Service 테스트")
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JobPostingRepository jobPostingRepository;
    
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    
    @Mock
    private CertificateRequestRepository certificateRequestRepository;
    
    @Mock
    private SystemMetricsRepository systemMetricsRepository;
    
    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardService dashboardService;

    private User generalUser;
    private User companyUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        generalUser = new User("general@example.com", "password", "일반유저", UserType.GENERAL);
        companyUser = new User("company@example.com", "password", "기업유저", UserType.COMPANY);
        adminUser = new User("admin@example.com", "password", "관리자", UserType.ADMIN);
    }

    @Test
    @DisplayName("일반 유저 대시보드를 조회할 수 있다")
    void 일반_유저_대시보드를_조회할_수_있다() {
        // given
        Long userId = 1L;
        
        // 모의 데이터 설정
        given(userService.findUserById(userId)).willReturn(generalUser);
        given(userRepository.countActiveUsersByType(UserType.GENERAL)).willReturn(1000L);
        given(jobApplicationRepository.countByStatusIn(any())).willReturn(300L);
        
        // 최적화된 통계 쿼리 모의 데이터 (Object[] 배열)
        Object[] applicationStats = {2L, 1L, 1L, 1L, 5L}; // pending, interview, rejected, accepted, total
        given(jobApplicationRepository.findApplicationStatisticsByUser(generalUser)).willReturn(applicationStats);

        // when
        GeneralUserDashboardDto dashboard = dashboardService.getGeneralUserDashboard(userId);

        // then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalEmploymentRate()).isNotNull();
        assertThat(dashboard.getMyJobScore()).isNotNull();
        assertThat(dashboard.getMyApplicationStatus()).isNotNull();
        assertThat(dashboard.getMyApplicationStatus().getTotalApplications()).isEqualTo(5);
    }

    @Test
    @DisplayName("기업 유저 대시보드를 조회할 수 있다")
    void 기업_유저_대시보드를_조회할_수_있다() {
        // given
        Long userId = 2L;
        
        // 모의 데이터 설정
        given(userService.findUserById(userId)).willReturn(companyUser);
        given(jobPostingRepository.countByCompanyUser(companyUser)).willReturn(10L);
        given(jobPostingRepository.countByCompanyUserAndStatus(any(), any())).willReturn(5L);
        given(jobApplicationRepository.countApplicationsByCompanyUser(companyUser)).willReturn(50L);
        given(jobApplicationRepository.countApplicationsByCompanyUserAndDateAfter(any(), any())).willReturn(15L);
        
        // 최적화된 통계 쿼리 모의 데이터 (Object[] 배열)
        Object[] companyStats = {10L, 5L, 3L, 2L, 10L, 30L}; // pending, documentPassed, interview, final, rejected, total
        given(jobApplicationRepository.findApplicationStatisticsByCompanyUser(companyUser)).willReturn(companyStats);
        
        // JobPosting 관련 통계 (빈 리스트로 설정)
        given(jobPostingRepository.findByCompanyUserWithApplicationCountOrderByCreatedAtDesc(any(), any())).willReturn(Arrays.asList());
        given(jobPostingRepository.findByCompanyUserWithApplicationCountOrderByViewCountDesc(any(), any())).willReturn(Arrays.asList());

        // when
        CompanyUserDashboardDto dashboard = dashboardService.getCompanyUserDashboard(userId);

        // then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalJobPostings()).isEqualTo(10);
        assertThat(dashboard.getActiveJobPostings()).isEqualTo(5);
        assertThat(dashboard.getTotalApplications()).isEqualTo(50);
        assertThat(dashboard.getNewApplicationsThisWeek()).isEqualTo(15);
    }

    @Test
    @DisplayName("관리자 대시보드를 조회할 수 있다")
    void 관리자_대시보드를_조회할_수_있다() {
        // given
        // 최적화된 통계 쿼리 모의 데이터 (Object[] 배열)
        Object[] userStats = {1200L, 280L, 20L, 1500L}; // general, company, admin, total
        given(userRepository.findUserStatistics()).willReturn(userStats);
        
        Object[] newUserStats = {50L, 200L, 300L, 30L, 20L}; // today, week, month, todayGeneral, todayCompany
        given(userRepository.findNewUserStatistics(any(), any(), any())).willReturn(newUserStats);
        
        Object[] jobPostingStats = {500L, 300L, 200L, 100L}; // total, active, closed, thisWeek
        given(jobPostingRepository.findJobPostingStatistics(any())).willReturn(jobPostingStats);
        
        Object[] applicationStats = {2000L, 150L, 500L, 200L}; // total, thisWeek, pending, successful
        given(jobApplicationRepository.findAdminApplicationStatistics(any())).willReturn(applicationStats);
        
        // 시스템 메트릭스 모의 데이터
        given(systemMetricsRepository.findTotalApiCalls()).willReturn(10000L);
        given(systemMetricsRepository.findTodayActiveUsers()).willReturn(java.util.Optional.of(500L));
        given(systemMetricsRepository.findTodayErrorCount()).willReturn(java.util.Optional.of(5L));
        
        // 증명서 요청 모의 데이터
        CertificateRequest request = new CertificateRequest(generalUser, CertificateType.COMPLETION_CERTIFICATE, "취업용");
        given(certificateRequestRepository.findRecentPendingRequests(any()))
                .willReturn(Arrays.asList(request));

        // when
        AdminDashboardDto dashboard = dashboardService.getAdminDashboard();

        // then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getUserStatistics()).isNotNull();
        assertThat(dashboard.getUserStatistics().getTotalUsers()).isEqualTo(1500L);
        assertThat(dashboard.getUserStatistics().getGeneralUsers()).isEqualTo(1200L);
        assertThat(dashboard.getUserStatistics().getCompanyUsers()).isEqualTo(280L);
        assertThat(dashboard.getJobPostingStatistics()).isNotNull();
        assertThat(dashboard.getApplicationStatistics()).isNotNull();
        assertThat(dashboard.getCertificateRequests()).hasSize(1);
        assertThat(dashboard.getSystemStatistics()).isNotNull();
    }

    @Test
    @DisplayName("취업 점수를 계산할 수 있다")
    void 취업_점수를_계산할_수_있다() {
        // given
        User user = generalUser;

        // when
        Integer jobScore = dashboardService.calculateJobScore(user);

        // then
        assertThat(jobScore).isNotNull();
        assertThat(jobScore).isBetween(0, 100);
    }

    @Test
    @DisplayName("개인 분석 인사이트를 생성할 수 있다")
    void 개인_분석_인사이트를_생성할_수_있다() {
        // given
        User user = generalUser;

        // when
        GeneralUserDashboardDto.PersonalInsightDto insight = dashboardService.generatePersonalInsight(user);

        // then
        assertThat(insight).isNotNull();
        assertThat(insight.getRecommendations()).isNotEmpty();
        assertThat(insight.getSkillsToImprove()).isNotEmpty();
        assertThat(insight.getSuggestedActions()).isNotEmpty();
        assertThat(insight.getOverallFeedback()).isNotBlank();
    }

    @Test
    @DisplayName("전체 취업률을 계산할 수 있다")
    void 전체_취업률을_계산할_수_있다() {
        // given
        given(userRepository.countActiveUsersByType(UserType.GENERAL)).willReturn(1000L);
        given(jobApplicationRepository.countByStatusIn(any())).willReturn(300L);

        // when
        Double employmentRate = dashboardService.calculateOverallEmploymentRate();

        // then
        assertThat(employmentRate).isNotNull();
        assertThat(employmentRate).isBetween(0.0, 100.0);
    }
}