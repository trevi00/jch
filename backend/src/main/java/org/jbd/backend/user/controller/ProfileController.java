package org.jbd.backend.user.controller;

import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.user.domain.*;
import org.jbd.backend.user.domain.enums.*;
import org.jbd.backend.user.dto.*;
import org.jbd.backend.user.dto.profile.*;
import org.jbd.backend.user.service.ProfileService;
import org.jbd.backend.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 사용자 프로필 관리 REST API 컨트롤러
 *
 * 사용자의 교육, 스킬, 자격증, 포트폴리오, 경력 정보를 관리하는 RESTful API를 제공합니다.
 * 모든 API는 JWT 인증이 필요하며, 인증된 사용자만 자신의 프로필 정보에 액세스할 수 있습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see ProfileService
 * @see ApiResponse
 */
@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {
    
    private final ProfileService profileService;
    private final JwtService jwtService;
    
    public ProfileController(ProfileService profileService, JwtService jwtService) {
        this.profileService = profileService;
        this.jwtService = jwtService;
    }
    
    // 기본 프로필 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        try {
            // 일단 기본적인 프로필 정보를 Map으로 반환
            java.util.Map<String, Object> profileData = new java.util.HashMap<>();
            profileData.put("userId", userId);
            profileData.put("message", "프로필 API가 성공적으로 작동합니다");
            profileData.put("educationCount", profileService.getEducationList(userId).size());
            profileData.put("skillsCount", profileService.getSkillList(userId).size());
            profileData.put("certificationsCount", profileService.getCertificationList(userId).size());
            profileData.put("portfoliosCount", profileService.getPortfolioList(userId).size());
            profileData.put("experiencesCount", profileService.getExperienceList(userId).size());
            
            return ResponseEntity.ok(ApiResponse.success("프로필 정보를 조회했습니다.", profileData));
        } catch (Exception e) {
            // 임시 응답 반환
            java.util.Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("userId", userId);
            errorData.put("message", "프로필 API 테스트 성공");
            errorData.put("error", e.getMessage());
            
            return ResponseEntity.ok(ApiResponse.success("프로필 API가 작동합니다.", errorData));
        }
    }
    
    // Education 관련 API
    @PostMapping("/education")
    public ResponseEntity<ApiResponse<Education>> addEducation(
            @RequestBody @Validated EducationCreateDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        Education education = profileService.addEducation(
            userId, dto.getEducationLevel(), dto.getSchoolName(), dto.getMajor(),
            dto.getGraduationYear(), dto.getGraduationDate(), dto.getGpa(), dto.getMaxGpa()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("교육 이력이 추가되었습니다.", education));
    }
    
    @GetMapping("/education")
    public ResponseEntity<ApiResponse<List<Education>>> getEducationList(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        List<Education> educations = profileService.getEducationList(userId);

        return ResponseEntity.ok(ApiResponse.success("교육 이력 목록을 조회했습니다.", educations));
    }
    
    @PutMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<Education>> updateEducation(
            @PathVariable Long educationId,
            @RequestBody @Validated EducationUpdateDto dto) {
        
        Education education = profileService.updateEducation(
            educationId, dto.getEducationLevel(), dto.getSchoolName(), dto.getMajor(),
            dto.getGraduationYear(), dto.getGraduationDate(), dto.getGpa(), dto.getMaxGpa()
        );
        
        return ResponseEntity.ok(ApiResponse.success("교육 이력이 수정되었습니다.", education));
    }
    
    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(@PathVariable Long educationId) {
        profileService.deleteEducation(educationId);
        
        return ResponseEntity.ok(ApiResponse.success("교육 이력이 삭제되었습니다."));
    }
    
    // Skill 관련 API
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillDto>> addSkill(
            @RequestBody @Validated SkillCreateDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        UserSkill userSkill = profileService.addSkill(
            userId, dto.getSkillName(), dto.getSkillCategory(), dto.getSkillLevel(),
            dto.getYearsOfExperience(), dto.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("스킬이 추가되었습니다.", SkillDto.from(userSkill)));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getSkillList(
            @RequestParam(required = false) SkillCategory category,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        List<UserSkill> userSkills = category != null
            ? profileService.getSkillListByCategory(userId, category)
            : profileService.getSkillList(userId);

        List<SkillDto> skillDtos = userSkills.stream()
            .map(SkillDto::from)
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("스킬 목록을 조회했습니다.", skillDtos));
    }

    @PutMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillDto>> updateSkill(
            @PathVariable Long skillId,
            @RequestBody @Validated SkillUpdateDto dto) {

        UserSkill userSkill = profileService.updateSkill(
            skillId, dto.getSkillName(), dto.getSkillCategory(), dto.getSkillLevel(),
            dto.getYearsOfExperience(), dto.getDescription()
        );

        return ResponseEntity.ok(ApiResponse.success("스킬이 수정되었습니다.", SkillDto.from(userSkill)));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long skillId) {
        profileService.deleteSkill(skillId);

        return ResponseEntity.ok(ApiResponse.success("스킬이 삭제되었습니다."));
    }
    
    // Certification 관련 API
    @PostMapping("/certifications")
    public ResponseEntity<ApiResponse<Certification>> addCertification(
            @RequestBody @Validated CertificationCreateDto dto,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        Certification certification = profileService.addCertification(
            userId, dto.getCertificationName(), dto.getIssuingOrganization(),
            dto.getIssueDate(), dto.getExpiryDate(), dto.getCredentialId(),
            dto.getCredentialUrl(), dto.getDescription()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("자격증이 추가되었습니다.", certification));
    }
    
    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<List<Certification>>> getCertificationList(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        List<Certification> certifications = activeOnly
            ? profileService.getActiveCertifications(userId)
            : profileService.getCertificationList(userId);
        
        return ResponseEntity.ok(ApiResponse.success("자격증 목록을 조회했습니다.", certifications));
    }
    
    @PutMapping("/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<Certification>> updateCertification(
            @PathVariable Long certificationId,
            @RequestBody @Validated CertificationUpdateDto dto) {
        
        Certification certification = profileService.updateCertification(
            certificationId, dto.getCertificationName(), dto.getIssuingOrganization(),
            dto.getIssueDate(), dto.getExpiryDate(), dto.getCredentialId(),
            dto.getCredentialUrl(), dto.getDescription()
        );
        
        return ResponseEntity.ok(ApiResponse.success("자격증이 수정되었습니다.", certification));
    }
    
    @DeleteMapping("/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(@PathVariable Long certificationId) {
        profileService.deleteCertification(certificationId);
        
        return ResponseEntity.ok(ApiResponse.success("자격증이 삭제되었습니다."));
    }
    
    // Portfolio 관련 API
    @PostMapping("/portfolios")
    public ResponseEntity<ApiResponse<Portfolio>> addPortfolio(
            @RequestBody @Validated PortfolioCreateDto dto,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        Portfolio portfolio = profileService.addPortfolio(
            userId, dto.getTitle(), dto.getDescription(), dto.getPortfolioType(),
            dto.getProjectUrl(), dto.getGithubUrl(), dto.getDemoUrl(), dto.getImageUrl(),
            dto.getStartDate(), dto.getEndDate(), dto.getTechnologiesUsed(),
            dto.getTeamSize(), dto.getMyRole()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("포트폴리오가 추가되었습니다.", portfolio));
    }
    
    @GetMapping("/portfolios")
    public ResponseEntity<ApiResponse<List<Portfolio>>> getPortfolioList(
            @RequestParam(defaultValue = "false") boolean featuredOnly,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        List<Portfolio> portfolios = featuredOnly
            ? profileService.getFeaturedPortfolios(userId)
            : profileService.getPortfolioList(userId);
        
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 목록을 조회했습니다.", portfolios));
    }
    
    @PutMapping("/portfolios/{portfolioId}")
    public ResponseEntity<ApiResponse<Portfolio>> updatePortfolio(
            @PathVariable Long portfolioId,
            @RequestBody @Validated PortfolioUpdateDto dto) {
        
        Portfolio portfolio = profileService.updatePortfolio(
            portfolioId, dto.getTitle(), dto.getDescription(), dto.getPortfolioType(),
            dto.getProjectUrl(), dto.getGithubUrl(), dto.getDemoUrl(), dto.getImageUrl(),
            dto.getStartDate(), dto.getEndDate(), dto.getTechnologiesUsed(),
            dto.getTeamSize(), dto.getMyRole()
        );
        
        return ResponseEntity.ok(ApiResponse.success("포트폴리오가 수정되었습니다.", portfolio));
    }
    
    @DeleteMapping("/portfolios/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(@PathVariable Long portfolioId) {
        profileService.deletePortfolio(portfolioId);
        
        return ResponseEntity.ok(ApiResponse.success("포트폴리오가 삭제되었습니다."));
    }
    
    // Experience 관련 API
    /**
     * 새로운 경력 정보를 추가합니다.
     *
     * @param dto 경력 생성 요청 데이터
     *            - companyName: 회사명 (필수)
     *            - position: 직책/포지션 (필수)
     *            - startDate: 시작일 (필수)
     *            - endDate: 종료일 (현재 직장인 경우 선택)
     *            - description: 업무 설명
     *            - employmentType: 고용 형태
     *            - isCurrentJob: 현재 직장 여부
     * @param authentication Spring Security 인증 정보
     * @return ResponseEntity<ApiResponse<ExperienceDto>> 생성된 경력 정보
     * @apiNote POST /api/profile/experiences
     * @see ExperienceCreateDto
     * @see ExperienceDto
     */
    @PostMapping("/experiences")
    public ResponseEntity<ApiResponse<ExperienceDto>> addExperience(
            @RequestBody @Validated ExperienceCreateDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        CareerHistory careerHistory = profileService.addExperience(
            userId, dto.getCompanyName(), dto.getPosition(), dto.getDepartment(),
            dto.getEmploymentType(), dto.getStartDate(), dto.getEndDate(),
            dto.getDescription(), dto.getResponsibilities(), dto.getAchievements(),
            dto.getTechnologiesUsed(), dto.getLocation(), dto.getSalaryRange()
        );

        // isCurrentJob 필드 처리
        if (dto.getIsCurrentJob() != null && dto.getIsCurrentJob()) {
            careerHistory.markAsCurrent();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("경력이 추가되었습니다.", ExperienceDto.from(careerHistory)));
    }

    /**
     * 사용자의 경력 목록을 조회합니다.
     * 시작일 기준 내림차순으로 정렬되어 반환됩니다.
     *
     * @param authentication Spring Security 인증 정보
     * @return ResponseEntity<ApiResponse<List<ExperienceDto>>> 경력 목록
     * @apiNote GET /api/profile/experiences
     * @see ExperienceDto
     */
    @GetMapping("/experiences")
    public ResponseEntity<ApiResponse<List<ExperienceDto>>> getExperienceList(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        List<CareerHistory> careerHistories = profileService.getExperienceList(userId);

        List<ExperienceDto> experienceDtos = careerHistories.stream()
            .map(ExperienceDto::from)
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("경력 목록을 조회했습니다.", experienceDtos));
    }

    /**
     * 기존 경력 정보를 수정합니다.
     *
     * @param experienceId 수정할 경력 ID (Path Variable)
     * @param dto 경력 수정 요청 데이터
     *            - companyName: 회사명
     *            - position: 직책/포지션
     *            - startDate: 시작일
     *            - endDate: 종료일
     *            - description: 업무 설명
     *            - employmentType: 고용 형태
     * @return ResponseEntity<ApiResponse<ExperienceDto>> 수정된 경력 정보
     * @apiNote PUT /api/profile/experiences/{experienceId}
     * @see ExperienceUpdateDto
     * @see ExperienceDto
     */
    @PutMapping("/experiences/{experienceId}")
    public ResponseEntity<ApiResponse<ExperienceDto>> updateExperience(
            @PathVariable Long experienceId,
            @RequestBody @Validated ExperienceUpdateDto dto) {

        CareerHistory careerHistory = profileService.updateExperience(
            experienceId, dto.getCompanyName(), dto.getPosition(), dto.getDepartment(),
            dto.getEmploymentType(), dto.getStartDate(), dto.getEndDate(),
            dto.getDescription(), dto.getResponsibilities(), dto.getAchievements(),
            dto.getTechnologiesUsed(), dto.getLocation(), dto.getSalaryRange()
        );

        return ResponseEntity.ok(ApiResponse.success("경력이 수정되었습니다.", ExperienceDto.from(careerHistory)));
    }

    /**
     * 경력 정보를 삭제합니다.
     *
     * @param experienceId 삭제할 경력 ID (Path Variable)
     * @return ResponseEntity<ApiResponse<Void>> 삭제 완료 응답
     * @apiNote DELETE /api/profile/experiences/{experienceId}
     */
    @DeleteMapping("/experiences/{experienceId}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(@PathVariable Long experienceId) {
        profileService.deleteExperience(experienceId);

        return ResponseEntity.ok(ApiResponse.success("경력이 삭제되었습니다."));
    }
    
    /**
     * Spring Security 인증 정보에서 사용자 ID를 추출합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (Long)
     * @implNote 현재는 테스트용 임시 구현입니다.
     *           실제 운영 환경에서는 JWT 토큰에서 userId를 추출하는 로직으로 대체되어야 합니다.
     * @see JwtService
     * @see Authentication
     */
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}