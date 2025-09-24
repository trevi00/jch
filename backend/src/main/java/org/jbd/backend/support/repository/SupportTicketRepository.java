package org.jbd.backend.support.repository;

import org.jbd.backend.support.domain.SupportTicket;
import org.jbd.backend.support.domain.TicketStatus;
import org.jbd.backend.support.domain.TicketPriority;
import org.jbd.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // 사용자별 티켓 조회
    Page<SupportTicket> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 상태별 사용자 티켓 조회
    Page<SupportTicket> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, TicketStatus status, Pageable pageable);

    // 담당 관리자별 티켓 조회
    Page<SupportTicket> findByAssignedAdmin_IdOrderByCreatedAtDesc(Long adminId, Pageable pageable);

    // 상태별 티켓 조회
    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    // 특정 사용자의 티켓 수 계산
    long countByUser_Id(Long userId);

    // 특정 상태의 티켓 수 계산
    long countByStatus(TicketStatus status);

    // 사용자와 상태별 티켓 수 계산
    long countByUser_IdAndStatus(Long userId, TicketStatus status);

    // 카테고리별 검색
    @Query("SELECT t FROM SupportTicket t WHERE t.category = :category ORDER BY t.createdAt DESC")
    Page<SupportTicket> findByCategoryOrderByCreatedAtDesc(@Param("category") String category, Pageable pageable);

    // 키워드 검색
    @Query("SELECT t FROM SupportTicket t WHERE " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<SupportTicket> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 사용자별 키워드 검색
    @Query("SELECT t FROM SupportTicket t WHERE t.user.id = :userId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<SupportTicket> searchByUserIdAndKeyword(@Param("userId") Long userId,
                                                  @Param("keyword") String keyword,
                                                  Pageable pageable);

    // 우선순위별 조회
    @Query("SELECT t FROM SupportTicket t WHERE t.priority = :priority ORDER BY t.createdAt DESC")
    Page<SupportTicket> findByPriorityOrderByCreatedAtDesc(@Param("priority") TicketPriority priority,
                                                            Pageable pageable);

    // 미할당 티켓 조회
    @Query("SELECT t FROM SupportTicket t WHERE t.assignedAdmin IS NULL AND t.status = 'OPEN' " +
           "ORDER BY CASE WHEN t.priority = 'URGENT' THEN 0 " +
           "WHEN t.priority = 'HIGH' THEN 1 " +
           "WHEN t.priority = 'NORMAL' THEN 2 " +
           "ELSE 3 END, t.createdAt ASC")
    Page<SupportTicket> findUnassignedTickets(Pageable pageable);

    // 평균 만족도 계산
    @Query("SELECT AVG(t.satisfactionRating) FROM SupportTicket t WHERE t.satisfactionRating IS NOT NULL")
    Double calculateAverageSatisfactionRating();

    // 사용자별 평균 만족도
    @Query("SELECT AVG(t.satisfactionRating) FROM SupportTicket t WHERE t.user.id = :userId AND t.satisfactionRating IS NOT NULL")
    Double calculateUserAverageSatisfactionRating(@Param("userId") Long userId);

    // Service에서 필요한 User 엔티티 기반 메서드들
    Page<SupportTicket> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<SupportTicket> findByUserAndStatusOrderByCreatedAtDesc(User user, TicketStatus status, Pageable pageable);
    long countByUser(User user);
    long countByUserAndStatus(User user, TicketStatus status);

    @Query("SELECT t FROM SupportTicket t WHERE t.user = :user AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<SupportTicket> searchByUserAndKeyword(@Param("user") User user,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);
}