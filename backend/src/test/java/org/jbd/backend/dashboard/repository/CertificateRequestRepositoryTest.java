package org.jbd.backend.dashboard.repository;

import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.domain.CertificateType;
import org.jbd.backend.dashboard.domain.enums.RequestStatus;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("자격증 요청 Repository 테스트")
class CertificateRequestRepositoryTest {

    @Autowired
    private CertificateRequestRepository certificateRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CertificateRequest pendingRequest;
    private CertificateRequest approvedRequest;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User("test@example.com", "password123", "테스트 사용자", UserType.GENERAL);
        testUser = userRepository.save(testUser);

        // 대기중인 자격증 요청 생성
        pendingRequest = new CertificateRequest(testUser, CertificateType.COMPLETION_CERTIFICATE, "취업 준비");
        pendingRequest = certificateRequestRepository.save(pendingRequest);

        // 승인된 자격증 요청 생성
        approvedRequest = new CertificateRequest(testUser, CertificateType.ENROLLMENT_CERTIFICATE, "자기계발");
        approvedRequest.approve("승인되었습니다");
        approvedRequest = certificateRequestRepository.save(approvedRequest);
    }

    @Test
    @DisplayName("사용자별 자격증 요청 조회 - 성공")
    void findByUserOrderByCreatedAtDesc_Success() {
        // when
        List<CertificateRequest> requests = certificateRequestRepository
                .findByUserOrderByCreatedAtDesc(testUser);

        // then
        assertThat(requests).isNotEmpty();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getUser().getId()).isEqualTo(testUser.getId());
        // 생성일 기준 내림차순 정렬 확인
        assertThat(requests.get(0).getCreatedAt())
                .isAfterOrEqualTo(requests.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("상태별 자격증 요청 조회 - 성공")
    void findByStatus_Success() {
        // when
        List<CertificateRequest> pendingRequests = certificateRequestRepository
                .findByStatus(RequestStatus.PENDING);
        List<CertificateRequest> approvedRequests = certificateRequestRepository
                .findByStatus(RequestStatus.APPROVED);

        // then
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
        
        assertThat(approvedRequests).hasSize(1);
        assertThat(approvedRequests.get(0).getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("상태별 요청 개수 조회 - 성공")
    void countByStatus_Success() {
        // when
        Long pendingCount = certificateRequestRepository.countByStatus(RequestStatus.PENDING);
        Long approvedCount = certificateRequestRepository.countByStatus(RequestStatus.APPROVED);

        // then
        assertThat(pendingCount).isEqualTo(1L);
        assertThat(approvedCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 기간 내 자격증 요청 개수 조회 - 성공")
    void countByCreatedAtBetween_Success() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when
        Long count = certificateRequestRepository
                .countByCreatedAtBetween(startDate, endDate);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("오늘 생성된 자격증 요청 개수 조회 - 성공")
    void countTodayRequests_Success() {
        // given
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // when
        Long count = certificateRequestRepository.countTodayRequests(todayStart, todayEnd);

        // then
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("이번 주 생성된 자격증 요청 개수 조회 - 성공")
    void countThisWeekRequests_Success() {
        // given
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1);

        // when
        Long count = certificateRequestRepository.countThisWeekRequests(weekStart);

        // then
        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("최근 미처리 요청 목록 조회 - 성공")
    void findRecentPendingRequests_Success() {
        // when
        List<CertificateRequest> pendingRequests = certificateRequestRepository
                .findRecentPendingRequests(PageRequest.of(0, 10));

        // then
        assertThat(pendingRequests).isNotEmpty();
        assertThat(pendingRequests).allMatch(request -> 
                request.getStatus() == RequestStatus.PENDING);
        // 생성일 기준 오름차순 정렬 확인 (오래된 것 먼저)
        for (int i = 0; i < pendingRequests.size() - 1; i++) {
            assertThat(pendingRequests.get(i).getCreatedAt())
                    .isBeforeOrEqualTo(pendingRequests.get(i + 1).getCreatedAt());
        }
    }

    @Test
    @DisplayName("자격증 종류별 요청 통계 조회 - 성공")
    void findCertificateTypeStatistics_Success() {
        // when
        List<Object[]> statistics = certificateRequestRepository
                .findCertificateTypeStatistics();

        // then
        assertThat(statistics).isNotEmpty();
        for (Object[] stat : statistics) {
            assertThat(stat).hasSize(2);
            assertThat(stat[0]).isInstanceOf(CertificateType.class);
            assertThat(stat[1]).isInstanceOf(Number.class);
        }
    }

    @Test
    @DisplayName("월별 자격증 요청 통계 조회 - 성공")
    void findMonthlyStatistics_Success() {
        // given
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // when
        List<Object[]> statistics = certificateRequestRepository
                .findMonthlyStatistics(sixMonthsAgo);

        // then
        assertThat(statistics).isNotNull();
        for (Object[] stat : statistics) {
            if (stat != null) {
                assertThat(stat).hasSize(3);
                // year, month, count 순서
                assertThat(stat[0]).isInstanceOf(Number.class); // year
                assertThat(stat[1]).isInstanceOf(Number.class); // month
                assertThat(stat[2]).isInstanceOf(Number.class); // count
            }
        }
    }

    @Test
    @DisplayName("처리된 요청 개수 조회 - 성공")
    void countProcessedRequests_Success() {
        // when
        Long processedCount = certificateRequestRepository
                .countProcessedRequests();

        // then
        // 승인된 요청이 있으므로 1개 이상이어야 함
        assertThat(processedCount).isNotNull();
        assertThat(processedCount).isGreaterThanOrEqualTo(1L);
    }

    @Test
    @DisplayName("사용자별 특정 상태 요청 개수 조회 - 성공")
    void countByUserIdAndStatus_Success() {
        // when
        Long pendingCount = certificateRequestRepository
                .countByUserIdAndStatus(testUser.getId(), RequestStatus.PENDING);
        Long approvedCount = certificateRequestRepository
                .countByUserIdAndStatus(testUser.getId(), RequestStatus.APPROVED);

        // then
        assertThat(pendingCount).isEqualTo(1L);
        assertThat(approvedCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("상태별 요청을 생성일자 기준 내림차순으로 조회 - 성공")
    void findByStatusOrderByCreatedAtDesc_Success() {
        // 추가 테스트 데이터 생성
        CertificateRequest anotherPendingRequest = new CertificateRequest(testUser, CertificateType.TRANSCRIPT, "추가 테스트");
        certificateRequestRepository.save(anotherPendingRequest);

        // when
        List<CertificateRequest> pendingRequests = certificateRequestRepository
                .findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);

        // then
        assertThat(pendingRequests).hasSize(2);
        assertThat(pendingRequests.get(0).getCreatedAt())
                .isAfterOrEqualTo(pendingRequests.get(1).getCreatedAt());
        assertThat(pendingRequests).allMatch(request -> 
                request.getStatus() == RequestStatus.PENDING);
    }

    @Test
    @DisplayName("생성일자 범위 내 요청을 생성일자 기준 내림차순으로 조회 - 성공")
    void findByCreatedAtBetweenOrderByCreatedAtDesc_Success() {
        // given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // when
        List<CertificateRequest> requests = certificateRequestRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        // then
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getCreatedAt())
                .isAfterOrEqualTo(requests.get(1).getCreatedAt());
        assertThat(requests).allMatch(request -> 
                request.getCreatedAt().isAfter(start) && request.getCreatedAt().isBefore(end));
    }
}