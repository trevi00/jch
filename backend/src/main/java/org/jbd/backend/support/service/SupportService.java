package org.jbd.backend.support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.exception.EntityNotFoundException;
import org.jbd.backend.support.domain.SupportMessage;
import org.jbd.backend.support.domain.SupportTicket;
import org.jbd.backend.support.domain.TicketPriority;
import org.jbd.backend.support.domain.TicketStatus;
import org.jbd.backend.support.dto.SupportDto;
import org.jbd.backend.support.repository.SupportMessageRepository;
import org.jbd.backend.support.repository.SupportTicketRepository;
import org.jbd.backend.support.repository.FAQRepository;
import org.jbd.backend.support.domain.FAQ;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FAQRepository faqRepository;

    private User getCurrentUser(String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // 티켓 생성
    @Transactional
    public SupportDto.TicketResponse createTicket(SupportDto.CreateTicketRequest request, String authHeader) {
        User currentUser = getCurrentUser(authHeader);

        SupportTicket ticket = new SupportTicket(
                currentUser,
                request.getSubject(),
                request.getDescription(),
                request.getCategory(),
                TicketPriority.valueOf(request.getPriority())
        );

        SupportTicket savedTicket = ticketRepository.save(ticket);
        return convertToTicketResponse(savedTicket);
    }

    // 티켓 목록 조회 (사용자)
    public Page<SupportDto.TicketResponse> getMyTickets(String status, Pageable pageable, String authHeader) {
        User currentUser = getCurrentUser(authHeader);

        Page<SupportTicket> tickets;
        if (status != null && !status.isEmpty()) {
            tickets = ticketRepository.findByUserAndStatusOrderByCreatedAtDesc(
                currentUser, TicketStatus.valueOf(status), pageable);
        } else {
            tickets = ticketRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        }

        return tickets.map(this::convertToTicketResponse);
    }

    // 티켓 상세 조회
    public SupportDto.TicketResponse getTicket(Long ticketId, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인: 본인 티켓 또는 관리자
        if (!ticket.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getUserType().equals(UserType.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to view this ticket");
        }

        return convertToTicketResponse(ticket);
    }

    // 티켓 상태 업데이트
    @Transactional
    public SupportDto.TicketResponse updateTicketStatus(Long ticketId, String status, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인: 본인 티켓 또는 관리자
        if (!ticket.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getUserType().equals(UserType.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to update this ticket");
        }

        TicketStatus newStatus = TicketStatus.valueOf(status);

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.markResolved();
        } else if (newStatus == TicketStatus.CLOSED) {
            ticket.close();
        } else if (newStatus == TicketStatus.IN_PROGRESS) {
            ticket.markInProgress();
        }

        SupportTicket updatedTicket = ticketRepository.save(ticket);
        return convertToTicketResponse(updatedTicket);
    }

    // 티켓 종료
    @Transactional
    public SupportDto.TicketResponse closeTicket(Long ticketId, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인
        if (!ticket.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getUserType().equals(UserType.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to close this ticket");
        }

        ticket.close();
        SupportTicket updatedTicket = ticketRepository.save(ticket);
        return convertToTicketResponse(updatedTicket);
    }

    // 티켓 재오픈
    @Transactional
    public SupportDto.TicketResponse reopenTicket(Long ticketId, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인
        if (!ticket.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to reopen this ticket");
        }

        ticket.reopen();
        SupportTicket updatedTicket = ticketRepository.save(ticket);
        return convertToTicketResponse(updatedTicket);
    }

    // 메시지 조회
    public List<SupportDto.MessageResponse> getMessages(Long ticketId, String authHeader) {
        // 티켓 존재 및 권한 확인
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        if (!ticket.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getUserType().equals(UserType.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to view messages for this ticket");
        }

        List<SupportMessage> messages = messageRepository.findBySupportTicketIdOrderByCreatedAtAsc(ticketId);
        return messages.stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    // 메시지 전송
    @Transactional
    public SupportDto.MessageResponse sendMessage(Long ticketId, SupportDto.SendMessageRequest request, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인
        if (!ticket.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getUserType().equals(UserType.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to send messages for this ticket");
        }

        boolean isFromAdmin = currentUser.getUserType().equals(UserType.ADMIN);

        SupportMessage message = new SupportMessage(
                ticket,
                currentUser,
                request.getMessage(),
                isFromAdmin,
                request.getAttachmentUrl()
        );

        SupportMessage savedMessage = messageRepository.save(message);

        // 티켓에 메시지 추가
        ticket.addMessage(savedMessage);

        ticketRepository.save(ticket);

        return convertToMessageResponse(savedMessage);
    }

    // 만족도 평가 제출
    @Transactional
    public void submitSatisfactionRating(Long ticketId, Integer rating, String feedback, String authHeader) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser(authHeader);

        // 권한 확인: 본인 티켓만 평가 가능
        if (!ticket.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only rate your own tickets");
        }

        // 해결된 티켓만 평가 가능
        if (!ticket.isResolved() && !ticket.isClosed()) {
            throw new IllegalStateException("You can only rate resolved or closed tickets");
        }

        ticket.addSatisfactionRating(rating, feedback);
        ticketRepository.save(ticket);
    }

    // 통계 조회
    public SupportDto.TicketStats getTicketStats(String authHeader) {
        User currentUser = getCurrentUser(authHeader);

        return SupportDto.TicketStats.builder()
                .totalTickets(ticketRepository.countByUser(currentUser))
                .openTickets(ticketRepository.countByUserAndStatus(currentUser, TicketStatus.OPEN))
                .inProgressTickets(ticketRepository.countByUserAndStatus(currentUser, TicketStatus.IN_PROGRESS))
                .resolvedTickets(ticketRepository.countByUserAndStatus(currentUser, TicketStatus.RESOLVED))
                .closedTickets(ticketRepository.countByUserAndStatus(currentUser, TicketStatus.CLOSED))
                .averageResponseTime(0L) // 실제 구현 필요
                .satisfactionScore(ticketRepository.calculateUserAverageSatisfactionRating(currentUser.getId()))
                .build();
    }

    // 카테고리 목록 조회
    public List<SupportDto.CategoryResponse> getCategories() {
        return List.of(
                SupportDto.CategoryResponse.builder()
                        .value("technical")
                        .label("기술적 문제")
                        .description("시스템 오류, 버그 등")
                        .build(),
                SupportDto.CategoryResponse.builder()
                        .value("billing")
                        .label("결제 관련")
                        .description("결제, 환불, 구독 등")
                        .build(),
                SupportDto.CategoryResponse.builder()
                        .value("account")
                        .label("계정 관련")
                        .description("로그인, 비밀번호, 프로필 등")
                        .build(),
                SupportDto.CategoryResponse.builder()
                        .value("general")
                        .label("일반 문의")
                        .description("기타 일반적인 문의사항")
                        .build(),
                SupportDto.CategoryResponse.builder()
                        .value("feature")
                        .label("기능 요청")
                        .description("새로운 기능 제안")
                        .build()
        );
    }

    // FAQ 조회
    public List<SupportDto.FAQResponse> getFAQ(String category) {
        List<FAQ> faqs;

        if (category != null && !category.isEmpty()) {
            faqs = faqRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(category);
        } else {
            faqs = faqRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();
        }

        return faqs.stream()
                .map(this::convertToFAQResponse)
                .collect(Collectors.toList());
    }

    // FAQ 조회수 증가
    @Transactional
    public void incrementFAQViewCount(Long faqId) {
        faqRepository.incrementViewCount(faqId);
    }

    // FAQ 도움됨 증가
    @Transactional
    public void markFAQHelpful(Long faqId) {
        faqRepository.incrementHelpfulCount(faqId);
    }

    // FAQ 검색
    public List<SupportDto.FAQResponse> searchFAQ(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getFAQ(null);
        }

        List<FAQ> faqs = faqRepository.searchByKeyword(keyword.trim());
        return faqs.stream()
                .map(this::convertToFAQResponse)
                .collect(Collectors.toList());
    }

    // 티켓 검색
    public Page<SupportDto.TicketResponse> searchTickets(String query, String category, String status, Pageable pageable, String authHeader) {
        User currentUser = getCurrentUser(authHeader);

        Page<SupportTicket> tickets = ticketRepository.searchByUserAndKeyword(
                currentUser, query, pageable);

        return tickets.map(this::convertToTicketResponse);
    }

    // Helper methods
    private SupportDto.TicketResponse convertToTicketResponse(SupportTicket ticket) {
        String adminName = null;
        if (ticket.getAssignedAdmin() != null) {
            adminName = ticket.getAssignedAdmin().getName();
        }

        return SupportDto.TicketResponse.builder()
                .id(ticket.getId())
                .subject(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .assignedAdminName(adminName)
                .createdAt(ticket.getCreatedAt().toString())
                .updatedAt(ticket.getUpdatedAt().toString())
                .resolvedAt(ticket.getResolvedAt() != null ? ticket.getResolvedAt().toString() : null)
                .messageCount(ticket.getMessages().size())
                .satisfactionRating(ticket.getSatisfactionRating())
                .satisfactionFeedback(ticket.getSatisfactionFeedback())
                .build();
    }

    private SupportDto.MessageResponse convertToMessageResponse(SupportMessage message) {
        return SupportDto.MessageResponse.builder()
                .id(message.getId())
                .ticketId(message.getSupportTicket().getId())
                .senderName(message.getSender().getName())
                .message(message.getMessageContent())
                .isFromSupport(message.isFromAdmin())
                .isInternalNote(message.isInternalNote())
                .attachmentUrl(message.getAttachmentUrl())
                .createdAt(message.getCreatedAt().toString())
                .build();
    }

    private SupportDto.FAQResponse convertToFAQResponse(FAQ faq) {
        return SupportDto.FAQResponse.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .viewCount(faq.getViewCount())
                .helpful(faq.getHelpfulCount())
                .build();
    }
}