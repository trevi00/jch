package org.jbd.backend.support.repository;

import org.jbd.backend.support.domain.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    // 티켓별 메시지 조회 (시간순) - SupportTicket ID 기반
    List<SupportMessage> findBySupportTicket_IdOrderByCreatedAtAsc(Long ticketId);

    // SupportTicket 엔티티 기반 (Service에서 사용)
    List<SupportMessage> findBySupportTicketIdOrderByCreatedAtAsc(Long ticketId);

    // 티켓별 메시지 페이징 조회
    Page<SupportMessage> findBySupportTicket_IdOrderByCreatedAtDesc(Long ticketId, Pageable pageable);

    // 사용자별 메시지 조회
    List<SupportMessage> findBySender_IdOrderByCreatedAtDesc(Long senderId);

    // 티켓별 메시지 수 계산
    long countBySupportTicket_Id(Long ticketId);

    // SupportTicket 엔티티 기반 카운트
    long countBySupportTicketId(Long ticketId);

    // 지원팀 메시지만 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId AND m.isFromAdmin = true " +
           "ORDER BY m.createdAt DESC")
    List<SupportMessage> findSupportMessagesByTicketId(@Param("ticketId") Long ticketId);

    // 사용자 메시지만 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId AND m.isFromAdmin = false " +
           "ORDER BY m.createdAt DESC")
    List<SupportMessage> findUserMessagesByTicketId(@Param("ticketId") Long ticketId);

    // 내부 노트만 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId AND m.isInternalNote = true " +
           "ORDER BY m.createdAt DESC")
    List<SupportMessage> findInternalNotesByTicketId(@Param("ticketId") Long ticketId);

    // 첨부파일이 있는 메시지 조회
    List<SupportMessage> findBySupportTicket_IdAndAttachmentUrlIsNotNullOrderByCreatedAtDesc(Long ticketId);

    // 최근 메시지 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId " +
           "ORDER BY m.createdAt DESC")
    List<SupportMessage> findRecentMessagesByTicketId(@Param("ticketId") Long ticketId, Pageable pageable);

    // 티켓의 마지막 메시지 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    SupportMessage findLastMessageByTicketId(@Param("ticketId") Long ticketId);

    // 티켓별 고객의 마지막 메시지 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId AND m.isFromAdmin = false " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    SupportMessage findLastUserMessageByTicketId(@Param("ticketId") Long ticketId);

    // 티켓별 지원팀의 마지막 메시지 조회
    @Query("SELECT m FROM SupportMessage m WHERE m.supportTicket.id = :ticketId AND m.isFromAdmin = true " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    SupportMessage findLastSupportMessageByTicketId(@Param("ticketId") Long ticketId);
}