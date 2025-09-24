package org.jbd.backend.company.controller;

import jakarta.validation.Valid;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.company.dto.CompanyDashboardDto;
import org.jbd.backend.company.dto.CompanyProfileDto;
import org.jbd.backend.company.dto.CompanyUpdateDto;
import org.jbd.backend.company.service.CompanyDashboardService;
import org.jbd.backend.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 기업 프로필 관리 REST API 컨트롤러
 *
 * 기업 사용자의 회사 프로필 생성, 조회, 수정, 삭제 등의 기능을 제공합니다.
 * 기업 사용자로 전환된 사용자만 접근할 수 있으며, 회사 정보 관리를 위한
 * 전용 인터페이스를 제공합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see CompanyService
 * @see CompanyProfileDto
 * @see CompanyUpdateDto
 */
@RestController
@RequestMapping("/company")
public class CompanyController {

    /** 기업 프로필 비즈니스 로직을 처리하는 서비스 */
    private final CompanyService companyService;

    /** 기업 대시보드 데이터를 처리하는 서비스 */
    private final CompanyDashboardService companyDashboardService;

    /** JWT 토큰 관리 서비스 */
    private final JwtService jwtService;

    /**
     * CompanyController 생성자
     *
     * @param companyService 기업 서비스
     * @param companyDashboardService 기업 대시보드 서비스
     * @param jwtService JWT 토큰 서비스
     */
    public CompanyController(CompanyService companyService,
                           CompanyDashboardService companyDashboardService,
                           JwtService jwtService) {
        this.companyService = companyService;
        this.companyDashboardService = companyDashboardService;
        this.jwtService = jwtService;
    }

    /**
     * 기업 프로필 정보를 조회합니다.
     * 인증된 기업 사용자의 회사 프로필 정보를 반환합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<CompanyProfileDto>> 기업 프로필 정보
     * @apiNote GET /company/profile
     * @see CompanyProfileDto
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyProfileDto>> getCompanyProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        CompanyProfileDto profile = companyService.getCompanyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * 기업 프로필 정보를 수정합니다.
     * 기존 기업 프로필 정보를 업데이트합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @param updateDto 기업 정보 수정 데이터
     *                  - companyName: 회사명 (선택)
     *                  - industry: 산업군 (선택)
     *                  - description: 회사 소개 (선택)
     *                  - website: 회사 웹사이트 (선택)
     *                  - location: 회사 위치 (선택)
     * @return ResponseEntity<ApiResponse<CompanyProfileDto>> 수정된 기업 프로필 정보
     * @apiNote PUT /company/profile
     * @see CompanyUpdateDto
     * @see CompanyProfileDto
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyProfileDto>> updateCompanyProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CompanyUpdateDto updateDto
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        CompanyProfileDto updatedProfile = companyService.updateCompanyProfile(userId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("기업 프로필이 수정되었습니다.", updatedProfile));
    }

    /**
     * 새로운 기업 프로필을 생성합니다.
     * 기업 사용자로 전환 후 최초 회사 프로필을 등록할 때 사용합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @param updateDto 기업 정보 생성 데이터
     *                  - companyName: 회사명 (필수)
     *                  - industry: 산업군 (선택)
     *                  - description: 회사 소개 (선택)
     *                  - website: 회사 웹사이트 (선택)
     *                  - location: 회사 위치 (선택)
     * @return ResponseEntity<ApiResponse<CompanyProfileDto>> 생성된 기업 프로필 정보
     * @apiNote POST /company/profile
     * @see CompanyUpdateDto
     * @see CompanyProfileDto
     */
    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyProfileDto>> createCompanyProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CompanyUpdateDto updateDto
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        CompanyProfileDto profile = companyService.createCompanyProfile(userId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("기업 프로필이 생성되었습니다.", profile));
    }

    /**
     * 기업 프로필 존재 여부를 확인합니다.
     * 현재 사용자가 기업 프로필을 등록했는지 여부를 반환합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<Boolean>> 프로필 존재 여부 (true: 존재, false: 미존재)
     * @apiNote GET /company/profile/exists
     */
    @GetMapping("/profile/exists")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<Boolean>> hasCompanyProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        boolean hasProfile = companyService.hasCompanyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(hasProfile));
    }

    /**
     * 기업 프로필을 삭제합니다.
     * 등록된 기업 프로필을 영구적으로 삭제합니다. 복구할 수 없습니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<Void>> 삭제 완료 응답
     * @apiNote DELETE /company/profile
     */
    @DeleteMapping("/profile")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteCompanyProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        companyService.deleteCompanyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("기업 프로필이 삭제되었습니다."));
    }

    /**
     * 기업 대시보드 종합 정보를 조회합니다.
     * 기업의 채용 현황, 통계, 최근 지원자 정보 등을 포함한 대시보드 데이터를 반환합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<CompanyDashboardDto>> 대시보드 종합 정보
     * @apiNote GET /company/dashboard
     * @see CompanyDashboardDto
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated() and hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<CompanyDashboardDto>> getCompanyDashboard(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        CompanyDashboardDto dashboard = companyDashboardService.getCompanyDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}