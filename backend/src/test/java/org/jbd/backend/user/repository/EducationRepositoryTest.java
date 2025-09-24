package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Education;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.EducationLevel;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("교육 이력 Repository 테스트")
class EducationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EducationRepository educationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("교육 이력을 저장할 수 있다")
    void canSaveEducation() {
        // Given
        Education education = new Education(testUser, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 2023);
        education.updateEducationInfo(
            EducationLevel.BACHELOR,
            "테스트대학교",
            "컴퓨터공학",
            2023,
            LocalDate.of(2023, 2, 28),
            new BigDecimal("3.75"),
            new BigDecimal("4.00")
        );

        // When
        Education saved = educationRepository.save(education);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getEducationLevel()).isEqualTo(EducationLevel.BACHELOR);
        assertThat(saved.getSchoolName()).isEqualTo("테스트대학교");
        assertThat(saved.getMajor()).isEqualTo("컴퓨터공학");
        assertThat(saved.getGraduationYear()).isEqualTo(2023);
        assertThat(saved.getGpa()).isEqualTo(new BigDecimal("3.75"));
        assertThat(saved.getMaxGpa()).isEqualTo(new BigDecimal("4.00"));
    }

    @Test
    @DisplayName("사용자별 교육 이력을 조회할 수 있다")
    void canFindEducationsByUser() {
        // Given
        Education education1 = new Education(testUser, EducationLevel.HIGH_SCHOOL, "테스트고등학교", null, 2019);
        Education education2 = new Education(testUser, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 2023);
        
        entityManager.persist(education1);
        entityManager.persist(education2);
        entityManager.flush();

        // When
        List<Education> educations = educationRepository.findByUserIdOrderByGraduationYearDesc(testUser.getId());

        // Then
        assertThat(educations).hasSize(2);
        assertThat(educations.get(0).getGraduationYear()).isEqualTo(2023);
        assertThat(educations.get(1).getGraduationYear()).isEqualTo(2019);
    }

    @Test
    @DisplayName("현재 재학 중인 교육 이력을 조회할 수 있다")
    void canFindCurrentEducation() {
        // Given
        Education currentEducation = new Education(testUser, EducationLevel.MASTER, "대학원", "데이터사이언스", null);
        currentEducation.markAsCurrent();
        
        Education completedEducation = new Education(testUser, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 2023);
        
        entityManager.persist(currentEducation);
        entityManager.persist(completedEducation);
        entityManager.flush();

        // When
        Optional<Education> current = educationRepository.findByUserIdAndIsCurrentTrue(testUser.getId());

        // Then
        assertThat(current).isPresent();
        assertThat(current.get().getEducationLevel()).isEqualTo(EducationLevel.MASTER);
        assertThat(current.get().getIsCurrent()).isTrue();
    }

    @Test
    @DisplayName("특정 교육 수준의 이력을 조회할 수 있다")
    void canFindEducationsByLevel() {
        // Given
        Education bachelor1 = new Education(testUser, EducationLevel.BACHELOR, "대학교A", "컴퓨터공학", 2020);
        Education bachelor2 = new Education(testUser, EducationLevel.BACHELOR, "대학교B", "전기공학", 2021);
        Education master = new Education(testUser, EducationLevel.MASTER, "대학원", "데이터사이언스", 2023);
        
        entityManager.persist(bachelor1);
        entityManager.persist(bachelor2);
        entityManager.persist(master);
        entityManager.flush();

        // When
        List<Education> bachelors = educationRepository.findByUserIdAndEducationLevel(testUser.getId(), EducationLevel.BACHELOR);

        // Then
        assertThat(bachelors).hasSize(2);
        assertThat(bachelors).allMatch(edu -> edu.getEducationLevel() == EducationLevel.BACHELOR);
    }

    @Test
    @DisplayName("최고 학력을 조회할 수 있다")
    void canFindHighestEducation() {
        // Given
        Education highSchool = new Education(testUser, EducationLevel.HIGH_SCHOOL, "고등학교", null, 2015);
        Education bachelor = new Education(testUser, EducationLevel.BACHELOR, "대학교", "컴퓨터공학", 2019);
        Education master = new Education(testUser, EducationLevel.MASTER, "대학원", "데이터사이언스", 2021);
        
        entityManager.persist(highSchool);
        entityManager.persist(bachelor);
        entityManager.persist(master);
        entityManager.flush();

        // When
        Optional<Education> highest = educationRepository.findTopByUserIdOrderByEducationLevelDesc(testUser.getId());

        // Then
        assertThat(highest).isPresent();
        assertThat(highest.get().getEducationLevel()).isEqualTo(EducationLevel.MASTER);
    }

    @Test
    @DisplayName("교육 이력을 삭제할 수 있다")
    void canDeleteEducation() {
        // Given
        Education education = new Education(testUser, EducationLevel.BACHELOR, "테스트대학교", "컴퓨터공학", 2023);
        Education saved = entityManager.persistAndFlush(education);

        // When
        educationRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<Education> found = educationRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}