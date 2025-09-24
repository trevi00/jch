package org.jbd.backend.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;
import org.jbd.backend.dashboard.dto.CompanyUserDashboardDto;
import org.jbd.backend.dashboard.dto.GeneralUserDashboardDto;
import org.jbd.backend.dashboard.service.DashboardService;
import org.jbd.backend.user.domain.enums.UserType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getDashboard(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            String userTypeStr = jwtService.extractUserType(token.replace("Bearer ", ""));
            UserType userType = UserType.valueOf(userTypeStr);

            switch (userType) {
                case GENERAL:
                    GeneralUserDashboardDto generalDashboard = dashboardService.getGeneralUserDashboard(userId);
                    return ResponseEntity.ok(ApiResponse.success("일반 유저 대시보드 조회 성공", generalDashboard));
                    
                case COMPANY:
                    CompanyUserDashboardDto companyDashboard = dashboardService.getCompanyUserDashboard(userId);
                    return ResponseEntity.ok(ApiResponse.success("기업 유저 대시보드 조회 성공", companyDashboard));
                    
                case ADMIN:
                    AdminDashboardDto adminDashboard = dashboardService.getAdminDashboard();
                    return ResponseEntity.ok(ApiResponse.success("관리자 대시보드 조회 성공", adminDashboard));
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("지원하지 않는 사용자 유형입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/general")
    public ResponseEntity<ApiResponse<GeneralUserDashboardDto>> getGeneralUserDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            GeneralUserDashboardDto dashboard = dashboardService.getGeneralUserDashboard(userId);
            return ResponseEntity.ok(ApiResponse.success("일반 유저 대시보드 조회 성공", dashboard));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/company")
    public ResponseEntity<ApiResponse<CompanyUserDashboardDto>> getCompanyUserDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            CompanyUserDashboardDto dashboard = dashboardService.getCompanyUserDashboard(userId);
            return ResponseEntity.ok(ApiResponse.success("기업 유저 대시보드 조회 성공", dashboard));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard(
            @RequestHeader("Authorization") String token) {
        try {
            String userTypeStr = jwtService.extractUserType(token.replace("Bearer ", ""));
            UserType userType = UserType.valueOf(userTypeStr);
            
            if (userType != UserType.ADMIN) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("관리자만 접근할 수 있습니다."));
            }
            
            AdminDashboardDto dashboard = dashboardService.getAdminDashboard();
            return ResponseEntity.ok(ApiResponse.success("관리자 대시보드 조회 성공", dashboard));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/employment-rate")
    public ResponseEntity<ApiResponse<Double>> getOverallEmploymentRate() {
        Double employmentRate = dashboardService.calculateOverallEmploymentRate();
        return ResponseEntity.ok(ApiResponse.success("전체 취업률 조회 성공", employmentRate));
    }

    @GetMapping("/job-score")
    public ResponseEntity<ApiResponse<Integer>> getMyJobScore(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            // UserService를 통해 User 엔티티를 가져와야 하므로, 
            // 실제로는 DashboardService에서 userId만으로 계산할 수 있도록 수정 필요
            // 현재는 간단히 응답만 반환
            return ResponseEntity.ok(ApiResponse.success("취업 점수 조회는 대시보드에서 확인하세요", 0));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }
}