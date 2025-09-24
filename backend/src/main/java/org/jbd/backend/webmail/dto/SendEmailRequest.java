package org.jbd.backend.webmail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    
    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String to;
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String subject;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 10000, message = "내용은 10,000자를 초과할 수 없습니다")
    private String content;
    
    // 번역 관련 필드
    private boolean translationNeeded = false;
    private String sourceLanguage = "ko";
    private String targetLanguage = "en";
    private String documentType = "email";
    
    // 발신자 정보 (현재 로그인한 사용자의 이메일)
    private String senderEmail;
    private String senderName;
    
    // From 필드 (선택사항 - 입력 시 본문에 포함)
    private String from;
}