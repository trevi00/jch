package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Certification;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("자격증 Repository 테스트")
class CertificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CertificationRepository certificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("자격증을 저장할 수 있다")
    void canSaveCertification() {
        // Given
        Certification certification = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        certification.updateCertification(
            "정보처리기사",
            "한국산업인력공단",
            LocalDate.of(2023, 5, 15),
            null,
            "23202345678",
            "https://www.q-net.or.kr",
            "정보처리 전반에 관한 전문 지식"
        );

        // When
        Certification saved = certificationRepository.save(certification);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getCertificationName()).isEqualTo("정보처리기사");
        assertThat(saved.getIssuingOrganization()).isEqualTo("한국산업인력공단");
        assertThat(saved.getIssueDate()).isEqualTo(LocalDate.of(2023, 5, 15));
        assertThat(saved.getCredentialId()).isEqualTo("23202345678");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("사용자별 자격증을 조회할 수 있다")
    void canFindCertificationsByUser() {
        // Given
        Certification cert1 = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        Certification cert2 = new Certification(testUser, "SQLD", "한국데이터진흥원", LocalDate.of(2023, 7, 20));
        
        entityManager.persist(cert1);
        entityManager.persist(cert2);
        entityManager.flush();

        // When
        List<Certification> certifications = certificationRepository.findByUserIdOrderByIssueDateDesc(testUser.getId());

        // Then
        assertThat(certifications).hasSize(2);
        assertThat(certifications.get(0).getIssueDate()).isEqualTo(LocalDate.of(2023, 7, 20));
        assertThat(certifications.get(1).getIssueDate()).isEqualTo(LocalDate.of(2023, 5, 15));
    }

    @Test
    @DisplayName("활성화된 자격증만 조회할 수 있다")
    void canFindActiveCertifications() {
        // Given
        Certification activeCert = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        Certification inactiveCert = new Certification(testUser, "SQLD", "한국데이터진흥원", LocalDate.of(2023, 7, 20));
        inactiveCert.deactivate();
        
        entityManager.persist(activeCert);
        entityManager.persist(inactiveCert);
        entityManager.flush();

        // When
        List<Certification> activeCertifications = certificationRepository.findByUserIdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(activeCertifications).hasSize(1);
        assertThat(activeCertifications.get(0).getCertificationName()).isEqualTo("정보처리기사");
    }

    @Test
    @DisplayName("만료된 자격증을 조회할 수 있다")
    void canFindExpiredCertifications() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate futureDate = LocalDate.now().plusDays(1);
        
        Certification expiredCert = new Certification(testUser, "만료된자격증", "테스트기관", LocalDate.of(2020, 1, 1));
        expiredCert.updateCertification("만료된자격증", "테스트기관", LocalDate.of(2020, 1, 1), 
                                      pastDate, null, null, null);
        
        Certification validCert = new Certification(testUser, "유효한자격증", "테스트기관", LocalDate.of(2023, 1, 1));
        validCert.updateCertification("유효한자격증", "테스트기관", LocalDate.of(2023, 1, 1),
                                    futureDate, null, null, null);
        
        entityManager.persist(expiredCert);
        entityManager.persist(validCert);
        entityManager.flush();

        // When
        List<Certification> expiredCertifications = certificationRepository.findByUserIdAndExpiryDateBefore(
            testUser.getId(), LocalDate.now());

        // Then
        assertThat(expiredCertifications).hasSize(1);
        assertThat(expiredCertifications.get(0).getCertificationName()).isEqualTo("만료된자격증");
    }

    @Test
    @DisplayName("발급기관별 자격증을 조회할 수 있다")
    void canFindCertificationsByIssuer() {
        // Given
        Certification cert1 = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        Certification cert2 = new Certification(testUser, "컴활1급", "한국산업인력공단", LocalDate.of(2023, 6, 20));
        Certification cert3 = new Certification(testUser, "SQLD", "한국데이터진흥원", LocalDate.of(2023, 7, 20));
        
        entityManager.persist(cert1);
        entityManager.persist(cert2);
        entityManager.persist(cert3);
        entityManager.flush();

        // When
        List<Certification> kosaaCerts = certificationRepository.findByUserIdAndIssuingOrganization(
            testUser.getId(), "한국산업인력공단");

        // Then
        assertThat(kosaaCerts).hasSize(2);
        assertThat(kosaaCerts).extracting("certificationName")
                             .containsExactlyInAnyOrder("정보처리기사", "컴활1급");
    }

    @Test
    @DisplayName("자격증을 삭제할 수 있다")
    void canDeleteCertification() {
        // Given
        Certification certification = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        Certification saved = entityManager.persistAndFlush(certification);

        // When
        certificationRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<Certification> found = certificationRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("자격증 개수를 조회할 수 있다")
    void canCountCertifications() {
        // Given
        Certification cert1 = new Certification(testUser, "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 5, 15));
        Certification cert2 = new Certification(testUser, "SQLD", "한국데이터진흥원", LocalDate.of(2023, 7, 20));
        
        entityManager.persist(cert1);
        entityManager.persist(cert2);
        entityManager.flush();

        // When
        long count = certificationRepository.countByUserIdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }
}