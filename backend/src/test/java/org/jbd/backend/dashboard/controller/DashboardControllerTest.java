package org.jbd.backend.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.exception.GlobalExceptionHandler;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;
import org.jbd.backend.dashboard.dto.CompanyUserDashboardDto;
import org.jbd.backend.dashboard.dto.GeneralUserDashboardDto;
import org.jbd.backend.dashboard.service.DashboardService;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 Controller 테스트")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("일반 유저 대시보드를 조회할 수 있다")
    void 일반_유저_대시보드를_조회할_수_있다() throws Exception {
        // given
        String token = "Bearer valid-jwt-token";
        Long userId = 1L;
        
        GeneralUserDashboardDto dashboardDto = GeneralUserDashboardDto.builder()
                .totalEmploymentRate(75.5)
                .myJobScore(85)
                .myApplicationStatus(GeneralUserDashboardDto.MyApplicationStatusDto.builder()
                        .totalApplications(10)
                        .pendingApplications(3)
                        .interviewApplications(2)
                        .rejectedApplications(3)
                        .acceptedApplications(2)
                        .build())
                .jobFieldEmployments(Arrays.asList(
                        GeneralUserDashboardDto.JobFieldEmploymentDto.builder()
                                .jobField("백엔드 개발")
                                .employmentRate(80.0)
                                .totalApplicants(100)
                                .employedCount(80)
                                .build()
                ))
                .personalInsight(GeneralUserDashboardDto.PersonalInsightDto.builder()
                        .recommendations(Arrays.asList("포트폴리오를 추가하세요"))
                        .skillsToImprove(Arrays.asList("Spring Boot"))
                        .suggestedActions(Arrays.asList("이력서 업데이트"))
                        .overallFeedback("좋은 진전을 보이고 있습니다")
                        .build())
                .quickActions(GeneralUserDashboardDto.QuickActionsDto.builder()
                        .mockInterviewUrl("/ai/interview")
                        .customJobPostingsUrl("/job-postings/recommended")
                        .profileUpdateUrl("/profile/edit")
                        .resumeGeneratorUrl("/resume/generator")
                        .build())
                .build();

        given(jwtService.extractUserId("valid-jwt-token")).willReturn(userId);
        given(jwtService.extractUserType("valid-jwt-token")).willReturn(UserType.GENERAL.name());
        given(dashboardService.getGeneralUserDashboard(userId)).willReturn(dashboardDto);

        // when & then
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmploymentRate").value(75.5))
                .andExpect(jsonPath("$.data.myJobScore").value(85))
                .andExpect(jsonPath("$.data.myApplicationStatus.totalApplications").value(10))
                .andExpect(jsonPath("$.data.jobFieldEmployments[0].jobField").value("백엔드 개발"))
                .andExpect(jsonPath("$.data.personalInsight.recommendations[0]").value("포트폴리오를 추가하세요"));
    }

    @Test
    @DisplayName("기업 유저 대시보드를 조회할 수 있다")
    void 기업_유저_대시보드를_조회할_수_있다() throws Exception {
        // given
        String token = "Bearer valid-jwt-token";
        Long userId = 2L;
        
        CompanyUserDashboardDto dashboardDto = CompanyUserDashboardDto.builder()
                .totalJobPostings(15)
                .activeJobPostings(8)
                .totalApplications(120)
                .newApplicationsThisWeek(25)
                .myJobPostings(Collections.emptyList())
                .popularJobPostings(Arrays.asList(
                        CompanyUserDashboardDto.PopularJobPostingDto.builder()
                                .jobPostingId(1L)
                                .title("백엔드 개발자 모집")
                                .companyName("테스트 회사")
                                .applicationCount(20)
                                .viewCount(150)
                                .status("PUBLISHED")
                                .build()
                ))
                .applicationStatistics(CompanyUserDashboardDto.ApplicationStatisticsDto.builder()
                        .pendingReview(10)
                        .documentPassed(5)
                        .interviewScheduled(3)
                        .finalPassed(2)
                        .rejected(5)
                        .averageApplicationsPerPosting(8.0)
                        .build())
                .quickActions(CompanyUserDashboardDto.CompanyQuickActionsDto.builder()
                        .createJobPostingUrl("/job-postings/create")
                        .manageApplicationsUrl("/applications/manage")
                        .companyProfileUrl("/company/profile")
                        .statisticsUrl("/company/statistics")
                        .build())
                .build();

        given(jwtService.extractUserId("valid-jwt-token")).willReturn(userId);
        given(jwtService.extractUserType("valid-jwt-token")).willReturn(UserType.COMPANY.name());
        given(dashboardService.getCompanyUserDashboard(userId)).willReturn(dashboardDto);

        // when & then
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalJobPostings").value(15))
                .andExpect(jsonPath("$.data.activeJobPostings").value(8))
                .andExpect(jsonPath("$.data.totalApplications").value(120))
                .andExpect(jsonPath("$.data.newApplicationsThisWeek").value(25))
                .andExpect(jsonPath("$.data.popularJobPostings[0].title").value("백엔드 개발자 모집"));
    }

    @Test
    @DisplayName("관리자 대시보드를 조회할 수 있다")
    void 관리자_대시보드를_조회할_수_있다() throws Exception {
        // given
        String token = "Bearer valid-jwt-token";
        Long userId = 3L;
        
        AdminDashboardDto dashboardDto = AdminDashboardDto.builder()
                .userStatistics(AdminDashboardDto.UserStatisticsDto.builder()
                        .totalUsers(1500L)
                        .generalUsers(1200L)
                        .companyUsers(280L)
                        .adminUsers(20L)
                        .activeUsers(1480L)
                        .inactiveUsers(20L)
                        .build())
                .newUserStatistics(AdminDashboardDto.NewUserStatisticsDto.builder()
                        .todayNewUsers(15L)
                        .thisWeekNewUsers(85L)
                        .thisMonthNewUsers(320L)
                        .todayGeneralUsers(12L)
                        .todayCompanyUsers(3L)
                        .build())
                .jobPostingStatistics(AdminDashboardDto.JobPostingStatisticsDto.builder()
                        .totalJobPostings(500L)
                        .activeJobPostings(200L)
                        .closedJobPostings(280L)
                        .thisWeekJobPostings(45L)
                        .averageApplicationsPerPosting(8.5)
                        .build())
                .applicationStatistics(AdminDashboardDto.ApplicationStatisticsDto.builder()
                        .totalApplications(4000L)
                        .thisWeekApplications(350L)
                        .pendingApplications(800L)
                        .successfulApplications(1200L)
                        .successRate(30.0)
                        .build())
                .certificateRequests(Collections.emptyList())
                .systemStatistics(AdminDashboardDto.SystemStatisticsDto.builder()
                        .totalApiCalls(50000L)
                        .dailyActiveUsers(1200L)
                        .systemUptime(99.9)
                        .errorCount(5)
                        .build())
                .build();

        given(jwtService.extractUserId("valid-jwt-token")).willReturn(userId);
        given(jwtService.extractUserType("valid-jwt-token")).willReturn(UserType.ADMIN.name());
        given(dashboardService.getAdminDashboard()).willReturn(dashboardDto);

        // when & then
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userStatistics.totalUsers").value(1500))
                .andExpect(jsonPath("$.data.userStatistics.generalUsers").value(1200))
                .andExpect(jsonPath("$.data.newUserStatistics.todayNewUsers").value(15))
                .andExpect(jsonPath("$.data.jobPostingStatistics.totalJobPostings").value(500))
                .andExpect(jsonPath("$.data.applicationStatistics.totalApplications").value(4000));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 대시보드에 접근할 수 없다")
    void 인증되지_않은_사용자는_대시보드에_접근할_수_없다() throws Exception {
        // given - malformed authorization header that will cause authentication failure
        String malformedToken = "NotBearer invalid-format";
        
        // when & then
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", malformedToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("잘못된 토큰으로는 대시보드에 접근할 수 없다")
    void 잘못된_토큰으로는_대시보드에_접근할_수_없다() throws Exception {
        // given
        String invalidToken = "Bearer invalid-token";
        given(jwtService.extractUserId("invalid-token")).willThrow(new RuntimeException("Invalid token"));

        // when & then
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}