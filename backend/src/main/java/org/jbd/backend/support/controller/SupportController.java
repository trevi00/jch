package org.jbd.backend.support.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.support.dto.SupportDto;
import org.jbd.backend.support.service.SupportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;
    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketResponse>> createTicket(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SupportDto.CreateTicketRequest request) {

        SupportDto.TicketResponse response = supportService.createTicket(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("티켓이 성공적으로 생성되었습니다", response));
    }

    @GetMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SupportDto.TicketResponse>>> getMyTickets(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SupportDto.TicketResponse> tickets = supportService.getMyTickets(status, pageable, authHeader);
        return ResponseEntity.ok(ApiResponse.success("티켓 목록을 조회했습니다", tickets));
    }

    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketResponse>> getTicket(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId) {

        SupportDto.TicketResponse ticket = supportService.getTicket(ticketId, authHeader);
        return ResponseEntity.ok(ApiResponse.success("티켓 정보를 조회했습니다", ticket));
    }

    @PutMapping("/tickets/{ticketId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketResponse>> updateTicketStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId,
            @Valid @RequestBody SupportDto.UpdateStatusRequest request) {

        SupportDto.TicketResponse ticket = supportService.updateTicketStatus(ticketId, request.getStatus(), authHeader);
        return ResponseEntity.ok(ApiResponse.success("티켓 상태가 변경되었습니다", ticket));
    }

    @PutMapping("/tickets/{ticketId}/close")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketResponse>> closeTicket(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId) {

        SupportDto.TicketResponse ticket = supportService.closeTicket(ticketId, authHeader);
        return ResponseEntity.ok(ApiResponse.success("티켓이 종료되었습니다", ticket));
    }

    @PutMapping("/tickets/{ticketId}/reopen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketResponse>> reopenTicket(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId) {

        SupportDto.TicketResponse ticket = supportService.reopenTicket(ticketId, authHeader);
        return ResponseEntity.ok(ApiResponse.success("티켓이 재오픈되었습니다", ticket));
    }

    @GetMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SupportDto.MessageResponse>>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId) {

        List<SupportDto.MessageResponse> messages = supportService.getMessages(ticketId, authHeader);
        return ResponseEntity.ok(ApiResponse.success("메시지 목록을 조회했습니다", messages));
    }

    @PostMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.MessageResponse>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId,
            @Valid @RequestBody SupportDto.SendMessageRequest request) {

        SupportDto.MessageResponse message = supportService.sendMessage(ticketId, request, authHeader);
        return ResponseEntity.ok(ApiResponse.success("메시지가 전송되었습니다", message));
    }

    @PostMapping("/tickets/{ticketId}/satisfaction")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> submitSatisfactionRating(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long ticketId,
            @Valid @RequestBody SupportDto.SatisfactionRequest request) {

        supportService.submitSatisfactionRating(ticketId, request.getRating(), request.getFeedback(), authHeader);
        return ResponseEntity.ok(ApiResponse.success("만족도 평가가 제출되었습니다"));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketStats>> getTicketStats(
            @RequestHeader("Authorization") String authHeader) {

        SupportDto.TicketStats stats = supportService.getTicketStats(authHeader);
        return ResponseEntity.ok(ApiResponse.success("통계를 조회했습니다", stats));
    }

    @GetMapping("/my-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.TicketStats>> getMyTicketStats(
            @RequestHeader("Authorization") String authHeader) {

        SupportDto.TicketStats stats = supportService.getTicketStats(authHeader);
        return ResponseEntity.ok(ApiResponse.success("내 통계를 조회했습니다", stats));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<SupportDto.CategoryResponse>>> getCategories() {

        List<SupportDto.CategoryResponse> categories = supportService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("카테고리 목록을 조회했습니다", categories));
    }

    @GetMapping("/faq")
    public ResponseEntity<ApiResponse<List<SupportDto.FAQResponse>>> getFAQ(
            @RequestParam(required = false) String category) {

        List<SupportDto.FAQResponse> faq = supportService.getFAQ(category);
        return ResponseEntity.ok(ApiResponse.success("FAQ를 조회했습니다", faq));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SupportDto.TicketResponse>>> searchTickets(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SupportDto.TicketResponse> tickets = supportService.searchTickets(q, category, status, pageable, authHeader);
        return ResponseEntity.ok(ApiResponse.success("검색 결과를 조회했습니다", tickets));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupportDto.FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {

        // 업로드 폴더가 없으면 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 업로드할 파일의 경로
        Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());

        // 파일 저장
        file.transferTo(filePath);

        // 응답 객체 생성
        SupportDto.FileUploadResponse response = SupportDto.FileUploadResponse.builder()
                .fileUrl("/uploads/" + file.getOriginalFilename()) // 파일 접근 경로
                .fileName(file.getOriginalFilename())  // 파일 이름
                .fileSize(file.getSize())  // 파일 크기
                .build();

        return ResponseEntity.ok(ApiResponse.success("파일이 업로드되었습니다", response));
    }

    // FAQ 관련 엔드포인트들

    // FAQ 조회수 증가
    @PostMapping("/faq/{faqId}/view")
    public ResponseEntity<ApiResponse<Void>> incrementFAQViewCount(@PathVariable Long faqId) {
        supportService.incrementFAQViewCount(faqId);
        return ResponseEntity.ok(ApiResponse.success("조회수가 증가되었습니다", null));
    }

    // FAQ 도움됨 증가
    @PostMapping("/faq/{faqId}/helpful")
    public ResponseEntity<ApiResponse<Void>> markFAQHelpful(@PathVariable Long faqId) {
        supportService.markFAQHelpful(faqId);
        return ResponseEntity.ok(ApiResponse.success("도움됨이 증가되었습니다", null));
    }

    // FAQ 검색
    @GetMapping("/faq/search")
    public ResponseEntity<ApiResponse<List<SupportDto.FAQResponse>>> searchFAQ(
            @RequestParam(required = false) String keyword) {
        List<SupportDto.FAQResponse> faqs = supportService.searchFAQ(keyword);
        return ResponseEntity.ok(ApiResponse.success("FAQ 검색이 완료되었습니다", faqs));
    }
}