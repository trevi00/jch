package org.jbd.backend.dashboard.repository;

import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.domain.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {
    
    /**
     * 특정 사용자의 자격증 요청 목록 조회
     */
    List<CertificateRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 특정 사용자의 자격증 요청 목록 조회 (User 엔티티 기준)
     */
    List<CertificateRequest> findByUserOrderByCreatedAtDesc(org.jbd.backend.user.domain.User user);
    
    /**
     * 모든 자격증 요청을 생성일자 기준 내림차순으로 조회 (페이징)
     */
    org.springframework.data.domain.Page<CertificateRequest> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 상태별 자격증 요청 조회
     */
    List<CertificateRequest> findByStatus(RequestStatus status);
    
    /**
     * 관리자용: 처리 대기중인 요청 목록 (페이징)
     */
    Page<CertificateRequest> findByStatusOrderByCreatedAtAsc(RequestStatus status, Pageable pageable);
    
    /**
     * 특정 기간 내 자격증 요청 개수 조회
     */
    @Query("SELECT COUNT(cr) FROM CertificateRequest cr WHERE cr.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 상태별 요청 개수 조회
     */
    Long countByStatus(RequestStatus status);
    
    /**
     * 특정 사용자의 특정 상태 요청 개수
     */
    Long countByUserIdAndStatus(Long userId, RequestStatus status);
    
    /**
     * 오늘 생성된 자격증 요청 개수
     */
    @Query("SELECT COUNT(cr) FROM CertificateRequest cr WHERE cr.createdAt >= :todayStart AND cr.createdAt < :todayEnd")
    Long countTodayRequests(@Param("todayStart") LocalDateTime todayStart, @Param("todayEnd") LocalDateTime todayEnd);
    
    /**
     * 이번 주 생성된 자격증 요청 개수
     */
    @Query("SELECT COUNT(cr) FROM CertificateRequest cr WHERE cr.createdAt >= :weekStart")
    Long countThisWeekRequests(@Param("weekStart") LocalDateTime weekStart);
    
    /**
     * 관리자용: 최근 미처리 요청 목록
     */
    @Query("SELECT cr FROM CertificateRequest cr WHERE cr.status = 'PENDING' ORDER BY cr.createdAt ASC")
    List<CertificateRequest> findRecentPendingRequests(Pageable pageable);
    
    /**
     * 자격증 종류별 요청 통계
     */
    @Query("SELECT cr.certificateType, COUNT(cr) FROM CertificateRequest cr GROUP BY cr.certificateType ORDER BY COUNT(cr) DESC")
    List<Object[]> findCertificateTypeStatistics();
    
    /**
     * 월별 자격증 요청 통계 (최근 6개월)
     */
    @Query("SELECT EXTRACT(year FROM cr.createdAt) as year, EXTRACT(month FROM cr.createdAt) as month, COUNT(cr) as count " +
           "FROM CertificateRequest cr " +
           "WHERE cr.createdAt >= :sixMonthsAgo " +
           "GROUP BY EXTRACT(year FROM cr.createdAt), EXTRACT(month FROM cr.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> findMonthlyStatistics(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
    
    /**
     * 처리된 요청 개수 조회 (통계용)
     */
    @Query("SELECT COUNT(cr) FROM CertificateRequest cr WHERE cr.processedAt IS NOT NULL")
    Long countProcessedRequests();
    
    /**
     * 처리된 요청 목록을 처리일자 기준 내림차순으로 조회
     */
    @Query("SELECT cr FROM CertificateRequest cr WHERE cr.processedAt IS NOT NULL ORDER BY cr.processedAt DESC")
    List<CertificateRequest> findProcessedRequestsOrderByProcessedAtDesc();
    
    /**
     * 상태와 생성일자 기준으로 조회 (관리자용)
     */
    List<CertificateRequest> findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(RequestStatus status, LocalDateTime createdAfter);
    
    /**
     * 상태별 요청을 생성일자 기준 내림차순으로 조회
     */
    List<CertificateRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    /**
     * 생성일자 범위 내 요청을 생성일자 기준 내림차순으로 조회
     */
    List<CertificateRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}