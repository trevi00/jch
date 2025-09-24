package org.jbd.backend.webmail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailResponse {
    
    private boolean success;
    private String message;
    private String messageId; // SendGrid Message ID
    private LocalDateTime sentAt;
    
    // 번역된 내용 (번역을 사용한 경우)
    private String originalContent;
    private String translatedContent;
    private boolean wasTranslated;
    
    public static SendEmailResponse success(String messageId, LocalDateTime sentAt) {
        return new SendEmailResponse(true, "이메일이 성공적으로 발송되었습니다", messageId, sentAt, null, null, false);
    }
    
    public static SendEmailResponse success(String messageId, LocalDateTime sentAt, String originalContent, String translatedContent) {
        return new SendEmailResponse(true, "이메일이 성공적으로 발송되었습니다", messageId, sentAt, originalContent, translatedContent, true);
    }
    
    public static SendEmailResponse failure(String message) {
        return new SendEmailResponse(false, message, null, null, null, null, false);
    }
}