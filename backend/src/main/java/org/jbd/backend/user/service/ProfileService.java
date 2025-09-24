package org.jbd.backend.user.service;

import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jbd.backend.user.domain.*;
import org.jbd.backend.user.domain.enums.*;
import org.jbd.backend.user.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 프로필 관리 서비스
 *
 * 교육, 스킬, 자격증, 포트폴리오, 경력 정보 등 사용자의 프로필 데이터를 관리합니다.
 * 모든 조회 메서드는 기본적으로 읽기 전용 트랜잭션으로 실행됩니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 */
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillMasterRepository skillMasterRepository;
    private final CertificationRepository certificationRepository;
    private final PortfolioRepository portfolioRepository;
    private final CareerHistoryRepository careerHistoryRepository;
    
    public ProfileService(UserRepository userRepository,
                         EducationRepository educationRepository,
                         UserSkillRepository userSkillRepository,
                         SkillMasterRepository skillMasterRepository,
                         CertificationRepository certificationRepository,
                         PortfolioRepository portfolioRepository,
                         CareerHistoryRepository careerHistoryRepository) {
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillMasterRepository = skillMasterRepository;
        this.certificationRepository = certificationRepository;
        this.portfolioRepository = portfolioRepository;
        this.careerHistoryRepository = careerHistoryRepository;
    }
    
    // Education 관련 메서드
    @Transactional
    public Education addEducation(Long userId, EducationLevel educationLevel, String schoolName, String major,
                                 Integer graduationYear, LocalDate graduationDate, BigDecimal gpa, BigDecimal maxGpa) {
        User user = getUserById(userId);
        
        Education education = new Education(user, educationLevel, schoolName, major, graduationYear);
        education.updateEducationInfo(educationLevel, schoolName, major, graduationYear, graduationDate, gpa, maxGpa);
        
        return educationRepository.save(education);
    }
    
    public List<Education> getEducationList(Long userId) {
        return educationRepository.findByUserIdOrderByGraduationYearDesc(userId);
    }
    
    @Transactional
    public Education updateEducation(Long educationId, EducationLevel educationLevel, String schoolName, String major,
                                   Integer graduationYear, LocalDate graduationDate, BigDecimal gpa, BigDecimal maxGpa) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EDUCATION_NOT_FOUND));
        
        education.updateEducationInfo(educationLevel, schoolName, major, graduationYear, graduationDate, gpa, maxGpa);
        
        return educationRepository.save(education);
    }
    
    @Transactional
    public void deleteEducation(Long educationId) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EDUCATION_NOT_FOUND));
        
        educationRepository.delete(education);
    }
    
    // Skill 관련 메서드
    @Transactional
    public UserSkill addSkill(Long userId, String skillName, SkillCategory skillCategory, SkillLevel skillLevel,
                         Integer yearsOfExperience, String description) {
        User user = getUserById(userId);

        // SkillMaster 찾기 또는 생성
        SkillMaster skillMaster = skillMasterRepository.findBySkillName(skillName)
                .orElseGet(() -> {
                    SkillMaster newSkill = new SkillMaster(skillName, skillCategory);
                    return skillMasterRepository.save(newSkill);
                });

        // 이미 해당 스킬이 있는지 확인
        Optional<UserSkill> existingSkill = userSkillRepository.findByUserIdAndSkillName(userId, skillName);
        if (existingSkill.isPresent()) {
            throw new IllegalStateException("이미 등록된 스킬입니다: " + skillName);
        }

        // UserSkill 생성 및 저장
        UserSkill userSkill = new UserSkill(user, skillMaster, skillLevel);
        if (yearsOfExperience != null) {
            userSkill.updateProficiency(skillLevel, yearsOfExperience);
        }

        logger.info("스킬 추가 완료: {} - {} (사용자: {})", skillName, skillLevel, userId);
        return userSkillRepository.save(userSkill);
    }

    public List<UserSkill> getSkillList(Long userId) {
        return userSkillRepository.findByUserId(userId);
    }

    public List<UserSkill> getSkillListByCategory(Long userId, SkillCategory skillCategory) {
        return userSkillRepository.findByUserId(userId); // 임시로 전체 반환
    }

    @Transactional
    public UserSkill updateSkill(Long userSkillId, String skillName, SkillCategory skillCategory, SkillLevel skillLevel,
                            Integer yearsOfExperience, String description) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        // 임시 업데이트 처리
        logger.info("스킬 업데이트: {} - {}", skillName, skillLevel);
        return userSkill;
    }

    @Transactional
    public void deleteSkill(Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SKILL_NOT_FOUND));

        userSkillRepository.delete(userSkill);
    }
    
    // Certification 관련 메서드
    @Transactional
    public Certification addCertification(Long userId, String certificationName, String issuingOrganization,
                                        LocalDate issueDate, LocalDate expiryDate, String credentialId,
                                        String credentialUrl, String description) {
        User user = getUserById(userId);
        
        Certification certification = new Certification(user, certificationName, issuingOrganization, issueDate);
        certification.updateCertification(certificationName, issuingOrganization, issueDate, expiryDate,
                                        credentialId, credentialUrl, description);
        
        return certificationRepository.save(certification);
    }
    
    public List<Certification> getCertificationList(Long userId) {
        return certificationRepository.findByUserIdOrderByIssueDateDesc(userId);
    }
    
    public List<Certification> getActiveCertifications(Long userId) {
        return certificationRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    @Transactional
    public Certification updateCertification(Long certificationId, String certificationName, String issuingOrganization,
                                           LocalDate issueDate, LocalDate expiryDate, String credentialId,
                                           String credentialUrl, String description) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATION_NOT_FOUND));
        
        certification.updateCertification(certificationName, issuingOrganization, issueDate, expiryDate,
                                        credentialId, credentialUrl, description);
        
        return certificationRepository.save(certification);
    }
    
    @Transactional
    public void deleteCertification(Long certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATION_NOT_FOUND));
        
        certificationRepository.delete(certification);
    }
    
    // Portfolio 관련 메서드
    @Transactional
    public Portfolio addPortfolio(Long userId, String title, String description, PortfolioType portfolioType,
                                String projectUrl, String githubUrl, String demoUrl, String imageUrl,
                                LocalDate startDate, LocalDate endDate, String technologiesUsed,
                                Integer teamSize, String myRole) {
        User user = getUserById(userId);
        
        Portfolio portfolio = new Portfolio(user, title, description);
        portfolio.updatePortfolio(title, description, projectUrl, githubUrl, startDate, endDate, technologiesUsed);
        
        return portfolioRepository.save(portfolio);
    }
    
    public List<Portfolio> getPortfolioList(Long userId) {
        return portfolioRepository.findByUserIdOrderByDisplayOrderAsc(userId);
    }
    
    public List<Portfolio> getFeaturedPortfolios(Long userId) {
        return portfolioRepository.findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(userId);
    }
    
    @Transactional
    public Portfolio updatePortfolio(Long portfolioId, String title, String description, PortfolioType portfolioType,
                                   String projectUrl, String githubUrl, String demoUrl, String imageUrl,
                                   LocalDate startDate, LocalDate endDate, String technologiesUsed,
                                   Integer teamSize, String myRole) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        portfolio.updatePortfolio(title, description, projectUrl, githubUrl, startDate, endDate, technologiesUsed);
        
        return portfolioRepository.save(portfolio);
    }
    
    @Transactional
    public void deletePortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        portfolioRepository.delete(portfolio);
    }
    
    // Experience 관련 메서드 (CareerHistory 사용)
    /**
     * 사용자의 경력 정보를 추가합니다.
     *
     * @param userId 사용자 ID
     * @param companyName 회사명 (필수)
     * @param position 직책/포지션 (필수)
     * @param department 부서명 (선택)
     * @param employmentType 고용 형태 (FULL_TIME, PART_TIME 등)
     * @param startDate 시작일 (필수)
     * @param endDate 종료일 (현재 직장인 경우 null)
     * @param description 업무 설명
     * @param responsibilities 담당 업무
     * @param achievements 성과/업적
     * @param technologiesUsed 사용 기술
     * @param location 근무 지역
     * @param salaryRange 급여 범위
     * @return 저장된 CareerHistory 엔티티
     * @throws BusinessException 사용자를 찾을 수 없는 경우 (USER_NOT_FOUND)
     */
    @Transactional
    public CareerHistory addExperience(Long userId, String companyName, String position, String department,
                                  EmploymentType employmentType, LocalDate startDate, LocalDate endDate,
                                  String description, String responsibilities, String achievements,
                                  String technologiesUsed, String location, String salaryRange) {
        User user = getUserById(userId);

        CareerHistory careerHistory = new CareerHistory(user, companyName, position, startDate);
        careerHistory.updateCareerInfo(companyName, position, department, startDate, endDate,
                                      description, achievements);
        careerHistory.setEmploymentType(employmentType);

        return careerHistoryRepository.save(careerHistory);
    }

    /**
     * 사용자의 경력 목록을 조회합니다.
     * 시작일 기준 내림차순으로 정렬되어 반환됩니다.
     *
     * @param userId 사용자 ID
     * @return 경력 목록 (시작일 기준 내림차순)
     */
    public List<CareerHistory> getExperienceList(Long userId) {
        return careerHistoryRepository.findByUserIdOrderByStartDateDesc(userId);
    }

    /**
     * 기존 경력 정보를 수정합니다.
     *
     * @param experienceId 수정할 경력 ID
     * @param companyName 회사명
     * @param position 직책/포지션
     * @param department 부서명
     * @param employmentType 고용 형태
     * @param startDate 시작일
     * @param endDate 종료일
     * @param description 업무 설명
     * @param responsibilities 담당 업무
     * @param achievements 성과/업적
     * @param technologiesUsed 사용 기술
     * @param location 근무 지역
     * @param salaryRange 급여 범위
     * @return 수정된 CareerHistory 엔티티
     * @throws BusinessException 경력 정보를 찾을 수 없는 경우 (EXPERIENCE_NOT_FOUND)
     */
    @Transactional
    public CareerHistory updateExperience(Long experienceId, String companyName, String position, String department,
                                     EmploymentType employmentType, LocalDate startDate, LocalDate endDate,
                                     String description, String responsibilities, String achievements,
                                     String technologiesUsed, String location, String salaryRange) {
        CareerHistory careerHistory = careerHistoryRepository.findById(experienceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPERIENCE_NOT_FOUND));

        careerHistory.updateCareerInfo(companyName, position, department, startDate, endDate,
                                      description, achievements);

        return careerHistoryRepository.save(careerHistory);
    }

    /**
     * 경력 정보를 삭제합니다.
     *
     * @param experienceId 삭제할 경력 ID
     * @throws BusinessException 경력 정보를 찾을 수 없는 경우 (EXPERIENCE_NOT_FOUND)
     */
    @Transactional
    public void deleteExperience(Long experienceId) {
        CareerHistory careerHistory = careerHistoryRepository.findById(experienceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPERIENCE_NOT_FOUND));

        careerHistoryRepository.delete(careerHistory);
    }
    
    // 유틸리티 메서드
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}