package org.jbd.backend.webmail.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.common.dto.PageResponse;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.webmail.domain.EmailHistory;
import org.jbd.backend.webmail.dto.SendEmailRequest;
import org.jbd.backend.webmail.dto.SendEmailResponse;
import org.jbd.backend.webmail.service.WebMailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webmail")
@RequiredArgsConstructor
@Slf4j
public class WebMailController {
    
    private final WebMailService webMailService;
    private final JwtService jwtService;
    
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<SendEmailResponse>> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            
            // 현재 로그인한 사용자 정보 설정
            String currentUserEmail = userDetails.getUsername();
            // 실제로는 User 엔티티에서 이름을 가져와야 하지만, 임시로 이메일에서 추출
            String currentUserName = currentUserEmail.split("@")[0];
            
            request.setSenderEmail(currentUserEmail);
            request.setSenderName(currentUserName);
            
            // SendGrid를 사용하여 이메일 발송
            log.info("SendGrid를 사용하여 이메일 발송: {} -> {}", currentUserEmail, request.getTo());
            SendEmailResponse response = webMailService.sendEmail(request, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(
                    ApiResponse.success("이메일이 성공적으로 발송되었습니다", response)
                );
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(response.getMessage())
                );
            }
            
        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("이메일 발송 중 시스템 오류가 발생했습니다")
            );
        }
    }
    
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<PageResponse<EmailHistory>>> getSentEmails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<EmailHistory> sentEmails = webMailService.getSentEmails(userId, pageable);
            
            return ResponseEntity.ok(
                ApiResponse.success("보낸편지함 조회 성공", new PageResponse<>(sentEmails))
            );
            
        } catch (Exception e) {
            log.error("보낸편지함 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("보낸편지함 조회 중 오류가 발생했습니다")
            );
        }
    }
    
    @GetMapping("/translated")
    public ResponseEntity<ApiResponse<List<EmailHistory>>> getTranslatedEmails(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            
            List<EmailHistory> translatedEmails = webMailService.getTranslatedEmails(userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("번역된 이메일 조회 성공", translatedEmails)
            );
            
        } catch (Exception e) {
            log.error("번역된 이메일 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("번역된 이메일 조회 중 오류가 발생했습니다")
            );
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<EmailStatsResponse>> getEmailStats(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            
            long totalSentCount = webMailService.getSentEmailCount(userId);
            long translatedCount = webMailService.getTranslatedEmails(userId).size();
            
            EmailStatsResponse stats = EmailStatsResponse.builder()
                .totalSentCount(totalSentCount)
                .translatedCount(translatedCount)
                .regularCount(totalSentCount - translatedCount)
                .build();
            
            return ResponseEntity.ok(
                ApiResponse.success("이메일 통계 조회 성공", stats)
            );
            
        } catch (Exception e) {
            log.error("이메일 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("이메일 통계 조회 중 오류가 발생했습니다")
            );
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class EmailStatsResponse {
        private long totalSentCount;    // 총 발송 이메일 수
        private long translatedCount;   // 번역된 이메일 수
        private long regularCount;      // 일반 이메일 수
    }
}