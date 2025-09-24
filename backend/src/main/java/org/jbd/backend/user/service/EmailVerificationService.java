package org.jbd.backend.user.service;

import org.jbd.backend.webmail.service.WebMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    private final WebMailService webMailService;
    private final Map<String, VerificationToken> tokenStore = new ConcurrentHashMap<>();

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@jbd.com}")
    private String fromEmail;

    public EmailVerificationService(WebMailService webMailService) {
        this.webMailService = webMailService;
    }

    public void sendVerificationEmail(String email, String token) {
        // 토큰 저장
        tokenStore.put(token, new VerificationToken(email, token, LocalDateTime.now().plusHours(24)));

        // 이메일 내용 생성
        String subject = "[JBD] 이메일 인증을 완료해주세요";
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        String content = String.format(
            "<h2>이메일 인증</h2>" +
            "<p>안녕하세요, JBD 회원가입을 환영합니다!</p>" +
            "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요:</p>" +
            "<a href='%s' style='display:inline-block;padding:10px 20px;background:#007bff;color:white;text-decoration:none;border-radius:5px'>이메일 인증하기</a>" +
            "<p>또는 다음 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
            "<p>%s</p>" +
            "<p>이 링크는 24시간 동안 유효합니다.</p>",
            verificationLink, verificationLink
        );

        // 이메일 발송 (간단한 로깅으로 대체)
        // webMailService.sendEmail(실제 구현 예정)
        logger.info("이메일 인증 메일 발송 완료: {}", email);
    }

    public void sendCompanyVerificationEmail(String companyEmail, String token) {
        // 토큰 저장
        tokenStore.put(token, new VerificationToken(companyEmail, token, LocalDateTime.now().plusHours(24)));

        // 이메일 내용 생성
        String subject = "[JBD] 회사 이메일 인증을 완료해주세요";
        String verificationLink = frontendUrl + "/verify-company-email?token=" + token;
        String content = String.format(
            "<h2>회사 이메일 인증</h2>" +
            "<p>안녕하세요, JBD 기업 회원 전환을 위한 회사 이메일 인증입니다.</p>" +
            "<p>아래 버튼을 클릭하여 회사 이메일 인증을 완료해주세요:</p>" +
            "<a href='%s' style='display:inline-block;padding:10px 20px;background:#28a745;color:white;text-decoration:none;border-radius:5px'>회사 이메일 인증하기</a>" +
            "<p>또는 다음 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
            "<p>%s</p>" +
            "<p>이 링크는 24시간 동안 유효합니다.</p>",
            verificationLink, verificationLink
        );

        // 이메일 발송 (간단한 로깅으로 대체)
        // webMailService.sendEmail(실제 구현 예정)
        logger.info("회사 이메일 인증 메일 발송 완료: {}", companyEmail);
    }

    public void sendPasswordResetEmail(String email, String token) {
        // 토큰 저장
        tokenStore.put(token, new VerificationToken(email, token, LocalDateTime.now().plusHours(1)));

        // 이메일 내용 생성
        String subject = "[JBD] 비밀번호 재설정";
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String content = String.format(
            "<h2>비밀번호 재설정</h2>" +
            "<p>비밀번호 재설정을 요청하셨습니다.</p>" +
            "<p>아래 버튼을 클릭하여 비밀번호를 재설정하세요:</p>" +
            "<a href='%s' style='display:inline-block;padding:10px 20px;background:#dc3545;color:white;text-decoration:none;border-radius:5px'>비밀번호 재설정</a>" +
            "<p>또는 다음 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
            "<p>%s</p>" +
            "<p>이 링크는 1시간 동안 유효합니다.</p>" +
            "<p>만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.</p>",
            resetLink, resetLink
        );

        // 이메일 발송 (간단한 로깅으로 대체)
        // webMailService.sendEmail(실제 구현 예정)
        logger.info("비밀번호 재설정 메일 발송 완료: {}", email);
    }

    public boolean verifyToken(String token) {
        VerificationToken verificationToken = tokenStore.get(token);
        if (verificationToken == null) {
            return false;
        }

        if (verificationToken.isExpired()) {
            tokenStore.remove(token);
            return false;
        }

        // 토큰 사용 후 제거
        tokenStore.remove(token);
        return true;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    // 내부 클래스: 인증 토큰 정보
    private static class VerificationToken {
        private final String email;
        private final String token;
        private final LocalDateTime expiryTime;

        public VerificationToken(String email, String token, LocalDateTime expiryTime) {
            this.email = email;
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}