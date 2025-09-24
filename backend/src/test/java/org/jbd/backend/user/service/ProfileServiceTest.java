package org.jbd.backend.user.service;

import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.*;
import org.jbd.backend.user.domain.enums.*;
import org.jbd.backend.user.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("프로필 서비스 테스트")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EducationRepository educationRepository;
    
    @Mock
    private SkillRepository skillRepository;
    
    @Mock
    private CertificationRepository certificationRepository;
    
    @Mock
    private PortfolioRepository portfolioRepository;
    
    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        // Reflection을 사용하여 ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, 1L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용하므로 예외를 무시
        }
    }

    @Test
    @DisplayName("교육 이력을 추가할 수 있다")
    void canAddEducation() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Education education = new Education(testUser, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 2023);
        when(educationRepository.save(any(Education.class))).thenReturn(education);

        // When
        Education result = profileService.addEducation(1L, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 
                                                     2023, LocalDate.of(2023, 2, 28), 
                                                     new BigDecimal("3.75"), new BigDecimal("4.00"));

        // Then
        assertThat(result.getEducationLevel()).isEqualTo(EducationLevel.BACHELOR);
        assertThat(result.getSchoolName()).isEqualTo("테스트대학교");
        assertThat(result.getMajor()).isEqualTo("컴퓨터공학");
        verify(educationRepository).save(any(Education.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 교육 이력 추가 시 예외가 발생한다")
    void shouldThrowExceptionWhenAddEducationToNonExistentUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> profileService.addEducation(1L, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 
                                                           2023, null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자의 교육 이력 목록을 조회할 수 있다")
    void canGetEducationList() {
        // Given
        Education education1 = new Education(testUser, EducationLevel.HIGH_SCHOOL, "고등학교", null, 2019);
        Education education2 = new Education(testUser, EducationLevel.BACHELOR, "대학교", "컴퓨터공학", 2023);
        List<Education> educations = Arrays.asList(education2, education1);
        
        when(educationRepository.findByUserIdOrderByGraduationYearDesc(1L)).thenReturn(educations);

        // When
        List<Education> result = profileService.getEducationList(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGraduationYear()).isEqualTo(2023);
        assertThat(result.get(1).getGraduationYear()).isEqualTo(2019);
    }

    @Test
    @DisplayName("스킬을 추가할 수 있다")
    void canAddSkill() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Skill skill = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);

        // When
        Skill result = profileService.addSkill(1L, "Java", SkillCategory.PROGRAMMING_LANGUAGE, 
                                             SkillLevel.ADVANCED, 3, "Spring Boot 개발 경험");

        // Then
        assertThat(result.getSkillName()).isEqualTo("Java");
        assertThat(result.getSkillCategory()).isEqualTo(SkillCategory.PROGRAMMING_LANGUAGE);
        assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("중복된 스킬 추가 시 예외가 발생한다")
    void shouldThrowExceptionWhenAddDuplicateSkill() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(skillRepository.findByUserIdAndSkillName(1L, "Java"))
                .thenReturn(Optional.of(new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED)));

        // When & Then
        assertThatThrownBy(() -> profileService.addSkill(1L, "Java", SkillCategory.PROGRAMMING_LANGUAGE, 
                                                        SkillLevel.ADVANCED, 3, "설명"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKILL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("사용자의 스킬 목록을 조회할 수 있다")
    void canGetSkillList() {
        // Given
        Skill skill1 = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill skill2 = new Skill(testUser, "Spring Boot", SkillCategory.FRAMEWORK, SkillLevel.INTERMEDIATE);
        List<Skill> skills = Arrays.asList(skill1, skill2);
        
        when(skillRepository.findByUserId(1L)).thenReturn(skills);

        // When
        List<Skill> result = profileService.getSkillList(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("skillName").containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    @DisplayName("자격증을 추가할 수 있다")
    void canAddCertification() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Certification certification = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        when(certificationRepository.save(any(Certification.class))).thenReturn(certification);

        // When
        Certification result = profileService.addCertification(1L, "정보처리기사", "한국산업인력공단", 
                                                             LocalDate.of(2023, 5, 15), null, 
                                                             "23202345678", "https://www.q-net.or.kr", "설명");

        // Then
        assertThat(result.getCertificationName()).isEqualTo("정보처리기사");
        assertThat(result.getIssuingOrganization()).isEqualTo("한국산업인력공단");
        verify(certificationRepository).save(any(Certification.class));
    }

    @Test
    @DisplayName("포트폴리오를 추가할 수 있다")
    void canAddPortfolio() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Portfolio portfolio = new Portfolio(testUser, "웹 프로젝트", "Spring Boot 기반 웹 애플리케이션", PortfolioType.WEB_APPLICATION);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);

        // When
        Portfolio result = profileService.addPortfolio(1L, "웹 프로젝트", "Spring Boot 기반 웹 애플리케이션", 
                                                     PortfolioType.WEB_APPLICATION, "https://project.com", 
                                                     "https://github.com/user/project", "https://demo.com", 
                                                     "image.jpg", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30),
                                                     "Java, Spring Boot, MySQL", 3, "백엔드 개발자");

        // Then
        assertThat(result.getTitle()).isEqualTo("웹 프로젝트");
        assertThat(result.getPortfolioType()).isEqualTo(PortfolioType.WEB_APPLICATION);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("경력을 추가할 수 있다")
    void canAddExperience() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Experience experience = new Experience(testUser, "테스트회사", "백엔드개발자", EmploymentType.FULL_TIME, LocalDate.of(2022, 1, 1));
        when(experienceRepository.save(any(Experience.class))).thenReturn(experience);

        // When
        Experience result = profileService.addExperience(1L, "테스트회사", "백엔드개발자", "개발팀", 
                                                        EmploymentType.FULL_TIME, LocalDate.of(2022, 1, 1), 
                                                        LocalDate.of(2023, 12, 31), "백엔드 API 개발", 
                                                        "REST API 설계 및 구현", "성능 개선 30%", 
                                                        "Java, Spring Boot", "서울", "3000-4000만원");

        // Then
        assertThat(result.getCompanyName()).isEqualTo("테스트회사");
        assertThat(result.getPosition()).isEqualTo("백엔드개발자");
        verify(experienceRepository).save(any(Experience.class));
    }

    @Test
    @DisplayName("스킬을 업데이트할 수 있다")
    void canUpdateSkill() {
        // Given
        Skill existingSkill = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.INTERMEDIATE);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(existingSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(existingSkill);

        // When
        Skill result = profileService.updateSkill(1L, "Java", SkillCategory.PROGRAMMING_LANGUAGE, 
                                                SkillLevel.ADVANCED, 5, "업데이트된 설명");

        // Then
        assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
        verify(skillRepository).save(existingSkill);
    }

    @Test
    @DisplayName("존재하지 않는 스킬 업데이트 시 예외가 발생한다")
    void shouldThrowExceptionWhenUpdateNonExistentSkill() {
        // Given
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> profileService.updateSkill(1L, "Java", SkillCategory.PROGRAMMING_LANGUAGE, 
                                                          SkillLevel.ADVANCED, 5, "설명"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKILL_NOT_FOUND);
    }

    @Test
    @DisplayName("스킬을 삭제할 수 있다")
    void canDeleteSkill() {
        // Given
        Skill existingSkill = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(existingSkill));

        // When
        profileService.deleteSkill(1L);

        // Then
        verify(skillRepository).delete(existingSkill);
    }
}