package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Skill;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.SkillCategory;
import org.jbd.backend.user.domain.enums.SkillLevel;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("스킬 Repository 테스트")
class SkillRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SkillRepository skillRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("스킬을 저장할 수 있다")
    void canSaveSkill() {
        // Given
        Skill skill = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        skill.updateSkill("Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED, 3, "Spring Boot 개발 경험");

        // When
        Skill saved = skillRepository.save(skill);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getSkillName()).isEqualTo("Java");
        assertThat(saved.getSkillCategory()).isEqualTo(SkillCategory.PROGRAMMING_LANGUAGE);
        assertThat(saved.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
        assertThat(saved.getYearsOfExperience()).isEqualTo(3);
        assertThat(saved.getDescription()).isEqualTo("Spring Boot 개발 경험");
    }

    @Test
    @DisplayName("사용자별 스킬을 조회할 수 있다")
    void canFindSkillsByUser() {
        // Given
        Skill java = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill spring = new Skill(testUser, "Spring Boot", SkillCategory.FRAMEWORK, SkillLevel.INTERMEDIATE);
        Skill mysql = new Skill(testUser, "MySQL", SkillCategory.DATABASE, SkillLevel.INTERMEDIATE);
        
        entityManager.persist(java);
        entityManager.persist(spring);
        entityManager.persist(mysql);
        entityManager.flush();

        // When
        List<Skill> skills = skillRepository.findByUserId(testUser.getId());

        // Then
        assertThat(skills).hasSize(3);
        assertThat(skills).extracting("skillName")
                          .containsExactlyInAnyOrder("Java", "Spring Boot", "MySQL");
    }

    @Test
    @DisplayName("카테고리별 스킬을 조회할 수 있다")
    void canFindSkillsByCategory() {
        // Given
        Skill java = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill python = new Skill(testUser, "Python", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.INTERMEDIATE);
        Skill spring = new Skill(testUser, "Spring Boot", SkillCategory.FRAMEWORK, SkillLevel.INTERMEDIATE);
        
        entityManager.persist(java);
        entityManager.persist(python);
        entityManager.persist(spring);
        entityManager.flush();

        // When
        List<Skill> programmingLanguages = skillRepository.findByUserIdAndSkillCategory(
            testUser.getId(), SkillCategory.PROGRAMMING_LANGUAGE);

        // Then
        assertThat(programmingLanguages).hasSize(2);
        assertThat(programmingLanguages).extracting("skillName")
                                       .containsExactlyInAnyOrder("Java", "Python");
    }

    @Test
    @DisplayName("스킬 레벨별로 조회할 수 있다")
    void canFindSkillsByLevel() {
        // Given
        Skill java = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill spring = new Skill(testUser, "Spring Boot", SkillCategory.FRAMEWORK, SkillLevel.ADVANCED);
        Skill mysql = new Skill(testUser, "MySQL", SkillCategory.DATABASE, SkillLevel.INTERMEDIATE);
        
        entityManager.persist(java);
        entityManager.persist(spring);
        entityManager.persist(mysql);
        entityManager.flush();

        // When
        List<Skill> advancedSkills = skillRepository.findByUserIdAndSkillLevel(
            testUser.getId(), SkillLevel.ADVANCED);

        // Then
        assertThat(advancedSkills).hasSize(2);
        assertThat(advancedSkills).extracting("skillName")
                                 .containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    @DisplayName("특정 스킬명으로 조회할 수 있다")
    void canFindSkillByName() {
        // Given
        Skill java = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        entityManager.persistAndFlush(java);

        // When
        Optional<Skill> found = skillRepository.findByUserIdAndSkillName(testUser.getId(), "Java");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSkillName()).isEqualTo("Java");
        assertThat(found.get().getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
    }

    @Test
    @DisplayName("스킬을 삭제할 수 있다")
    void canDeleteSkill() {
        // Given
        Skill skill = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill saved = entityManager.persistAndFlush(skill);

        // When
        skillRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<Skill> found = skillRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("카테고리별로 스킬 개수를 조회할 수 있다")
    void canCountSkillsByCategory() {
        // Given
        Skill java = new Skill(testUser, "Java", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.ADVANCED);
        Skill python = new Skill(testUser, "Python", SkillCategory.PROGRAMMING_LANGUAGE, SkillLevel.INTERMEDIATE);
        Skill spring = new Skill(testUser, "Spring Boot", SkillCategory.FRAMEWORK, SkillLevel.INTERMEDIATE);
        
        entityManager.persist(java);
        entityManager.persist(python);
        entityManager.persist(spring);
        entityManager.flush();

        // When
        long count = skillRepository.countByUserIdAndSkillCategory(testUser.getId(), SkillCategory.PROGRAMMING_LANGUAGE);

        // Then
        assertThat(count).isEqualTo(2);
    }
}