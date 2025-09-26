package org.jbd.backend.webmail.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.service.AITranslationService;
import org.jbd.backend.ai.dto.TranslationDto;
import org.jbd.backend.webmail.domain.EmailHistory;
import org.jbd.backend.webmail.domain.EmailStatus;
import org.jbd.backend.webmail.dto.SendEmailRequest;
import org.jbd.backend.webmail.dto.SendEmailResponse;
import org.jbd.backend.webmail.repository.EmailHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebMailService {
    
    @Value("${spring.mail.password:}")
    private String sendGridApiKey;
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
    @Value("${webmail.default-sender.email}")
    private String defaultSenderEmail;
    
    @Value("${webmail.default-sender.name}")
    private String defaultSenderName;
    
    @Value("${webmail.test-mode:false}")
    private boolean testMode;
    
    private final EmailHistoryRepository emailHistoryRepository;
    private final AITranslationService aiTranslationService;
    
    public SendEmailResponse sendEmail(SendEmailRequest request, Long userId) {
        try {
            String contentToSend = request.getContent();
            String originalContent = null;
            String translatedContent = null;
            boolean wasTranslated = false;
            
            // 번역이 필요한 경우 AI 번역 서비스 호출
            if (request.isTranslationNeeded()) {
                log.info("번역 요청: {} -> {}", request.getSourceLanguage(), request.getTargetLanguage());
                
                try {
                    TranslationDto.TranslateResponse translationResponse = 
                        aiTranslationService.translateText(
                            request.getContent(),
                            request.getTargetLanguage(),
                            request.getSourceLanguage(),
                            "email"
                        );
                    
                    if (translationResponse != null && translationResponse.isSuccess() && 
                        translationResponse.getData() != null && 
                        translationResponse.getData().getTranslation() != null &&
                        translationResponse.getData().getTranslation().getTranslatedText() != null &&
                        !translationResponse.getData().getTranslation().getTranslatedText().trim().isEmpty()) {
                        
                        originalContent = request.getContent();
                        translatedContent = translationResponse.getData().getTranslation().getTranslatedText();
                        contentToSend = translatedContent;
                        wasTranslated = true;
                        
                        log.info("번역 성공: {} 글자 -> {} 글자", 
                            originalContent.length(), translatedContent.length());
                    } else {
                        log.warn("번역 실패: {}. 원본 내용으로 발송합니다.", 
                            translationResponse != null ? translationResponse.getMessage() : "번역 응답이 null입니다");
                        contentToSend = request.getContent(); // 원본 내용으로 fallback
                    }
                } catch (Exception e) {
                    log.error("번역 중 오류 발생: {}. 원본 내용으로 발송합니다.", e.getMessage());
                    contentToSend = request.getContent(); // 원본 내용으로 fallback
                }
            }
            
            // from 필드가 있으면 본문에 포함
            if (request.getFrom() != null && !request.getFrom().trim().isEmpty()) {
                contentToSend = "From: " + request.getFrom() + "\n\n" + contentToSend;
            }
            
            // 이메일 발송 (test-mode 설정에 따라 Mock 또는 실제 SendGrid 사용)
            boolean isEmailSent = false;
            String messageId = "mock-" + System.currentTimeMillis();
            
            log.info("SendGrid API Key 상태: {}", sendGridApiKey == null ? "null" : 
                sendGridApiKey.trim().isEmpty() ? "empty" : "configured (length: " + sendGridApiKey.length() + ")");
            log.info("웹메일 테스트 모드: {}", testMode);
            
            if (testMode || sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
                // Mock 이메일 발송 (개발/테스트 환경)
                log.info("=== MOCK 이메일 발송 ===");
                log.info("From: {} <{}>", defaultSenderName, defaultSenderEmail);
                log.info("To: {}", request.getTo());
                log.info("Subject: {}", request.getSubject());
                log.info("Content: {}", contentToSend);
                log.info("Translation: {}", wasTranslated ? "Yes" : "No");
                if (wasTranslated) {
                    log.info("Original Language: {}", request.getSourceLanguage());
                    log.info("Target Language: {}", request.getTargetLanguage());
                }
                log.info("=====================");
                
                isEmailSent = true;
            } else {
                // 실제 SendGrid 이메일 발송 (고정된 검증된 발신자 사용)
                try {
                    Email from = new Email(defaultSenderEmail, defaultSenderName);
                    Email to = new Email(request.getTo());
                    Content content = new Content("text/html", formatEmailContent(contentToSend));
                    
                    Mail mail = new Mail(from, request.getSubject(), to, content);
                    
                    SendGrid sg = new SendGrid(sendGridApiKey);
                    Request sgRequest = new Request();
                    
                    sgRequest.setMethod(Method.POST);
                    sgRequest.setEndpoint("mail/send");
                    sgRequest.setBody(mail.build());
                    
                    Response response = sg.api(sgRequest);
                    
                    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        isEmailSent = true;
                        messageId = extractMessageId(response);
                    } else {
                        log.error("SendGrid 이메일 발송 실패: Status={}, Body={}", 
                            response.getStatusCode(), response.getBody());
                        return SendEmailResponse.failure("이메일 발송에 실패했습니다: " + response.getBody());
                    }
                } catch (IOException e) {
                    log.error("SendGrid API 호출 중 오류 발생", e);
                    return SendEmailResponse.failure("이메일 발송 중 시스템 오류가 발생했습니다.");
                }
            }
            
            if (isEmailSent) {
                // 발송 성공
                LocalDateTime sentAt = LocalDateTime.now();
                
                // 이메일 이력 저장 (실제 발신자 정보 사용)
                EmailHistory emailHistory = EmailHistory.builder()
                    .senderEmail(request.getSenderEmail()) // 실제 로그인한 사용자 이메일
                    .senderName(request.getSenderName())   // 실제 로그인한 사용자 이름
                    .recipientEmail(request.getTo())
                    .subject(request.getSubject())
                    .content(contentToSend)
                    .originalContent(originalContent)
                    .translatedContent(translatedContent)
                    .wasTranslated(wasTranslated)
                    .sourceLanguage(request.getSourceLanguage())
                    .targetLanguage(request.getTargetLanguage())
                    .documentType(request.getDocumentType())
                    .sendgridMessageId(messageId)
                    .status(EmailStatus.SENT)
                    .userId(userId)
                    .build();
                
                emailHistoryRepository.save(emailHistory);
                
                log.info("이메일 발송 성공: {} -> {}", defaultSenderEmail, request.getTo());
                
                if (wasTranslated) {
                    return SendEmailResponse.success(messageId, sentAt, originalContent, translatedContent);
                } else {
                    return SendEmailResponse.success(messageId, sentAt);
                }
                
            } else {
                log.error("이메일 발송에 실패했습니다");
                return SendEmailResponse.failure("이메일 발송에 실패했습니다");
            }
            
        } catch (Exception e) {
            log.error("이메일 발송 중 예상치 못한 오류 발생", e);
            return SendEmailResponse.failure("이메일 발송 중 오류가 발생했습니다.");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<EmailHistory> getSentEmails(Long userId, Pageable pageable) {
        return emailHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<EmailHistory> getTranslatedEmails(Long userId) {
        return emailHistoryRepository.findByUserIdAndWasTranslatedTrueOrderByCreatedAtDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public long getSentEmailCount(Long userId) {
        return emailHistoryRepository.countByUserId(userId);
    }
    
    private String formatEmailContent(String content) {
        // HTML 형식으로 이메일 내용 포맷팅
        return String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                %s
            </div>
            <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
            <p style="font-size: 12px; color: #888; text-align: center;">
                이 메일은 JBD 취업 지원 솔루션을 통해 발송되었습니다.
            </p>
            """, content.replace("\n", "<br>"));
    }
    
    private String extractMessageId(Response response) {
        // SendGrid Response에서 Message ID 추출
        try {
            String messageId = response.getHeaders().get("X-Message-Id");
            if (messageId != null && !messageId.trim().isEmpty()) {
                return messageId.trim();
            }
        } catch (Exception e) {
            log.debug("헤더에서 Message ID 추출 실패: {}", e.getMessage());
        }
        
        // 헤더에서 추출 실패 시 타임스탬프 기반 ID 생성
        return "jbd-" + System.currentTimeMillis();
    }
}