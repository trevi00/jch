package org.jbd.backend.webmail.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.service.AITranslationService;
import org.jbd.backend.ai.dto.TranslationDto;
import org.jbd.backend.webmail.domain.EmailHistory;
import org.jbd.backend.webmail.domain.EmailStatus;
import org.jbd.backend.webmail.dto.SendEmailRequest;
import org.jbd.backend.webmail.dto.SendEmailResponse;
import org.jbd.backend.webmail.repository.EmailHistoryRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GmailService {
    
    private static final String APPLICATION_NAME = "JBD Webmail Service";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final EmailHistoryRepository emailHistoryRepository;
    private final AITranslationService aiTranslationService;
    
    public SendEmailResponse sendEmailViaGmail(SendEmailRequest request, Long userId, String userEmail) {
        try {
            // OAuth2 클라이언트에서 액세스 토큰 가져오기
            OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient("google", userEmail);
            
            if (authorizedClient == null) {
                log.error("사용자 {}의 Google OAuth2 인증 정보를 찾을 수 없습니다", userEmail);
                return SendEmailResponse.failure("Google 계정 연동이 필요합니다. 다시 로그인해주세요.");
            }
            
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            if (accessToken == null || accessToken.getTokenValue() == null) {
                log.error("사용자 {}의 액세스 토큰이 유효하지 않습니다", userEmail);
                return SendEmailResponse.failure("Google 인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
            }
            
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
                        contentToSend = request.getContent();
                    }
                } catch (Exception e) {
                    log.error("번역 중 오류 발생: {}. 원본 내용으로 발송합니다.", e.getMessage());
                    contentToSend = request.getContent();
                }
            }
            
            // Gmail API 클라이언트 생성
            Gmail service = createGmailService(accessToken.getTokenValue());
            
            // 이메일 메시지 생성
            MimeMessage email = createEmail(request.getTo(), userEmail, request.getSubject(), contentToSend);
            Message message = createMessageWithEmail(email);
            
            // Gmail을 통해 이메일 발송
            message = service.users().messages().send("me", message).execute();
            
            if (message != null && message.getId() != null) {
                // 발송 성공
                LocalDateTime sentAt = LocalDateTime.now();
                
                // 이메일 히스토리 저장
                EmailHistory emailHistory = EmailHistory.builder()
                    .senderEmail(userEmail)
                    .senderName("JBD User") // 실제 사용자 이름으로 변경 가능
                    .recipientEmail(request.getTo())
                    .subject(request.getSubject())
                    .content(contentToSend)
                    .originalContent(originalContent)
                    .translatedContent(translatedContent)
                    .wasTranslated(wasTranslated)
                    .sourceLanguage(request.getSourceLanguage())
                    .targetLanguage(request.getTargetLanguage())
                    .documentType(request.getDocumentType())
                    .sendgridMessageId(message.getId()) // Gmail 메시지 ID 저장
                    .status(EmailStatus.SENT)
                    .userId(userId)
                    .build();
                
                emailHistoryRepository.save(emailHistory);
                
                log.info("Gmail을 통한 이메일 발송 성공: {} -> {}", userEmail, request.getTo());
                
                if (wasTranslated) {
                    return SendEmailResponse.success(message.getId(), sentAt, originalContent, translatedContent);
                } else {
                    return SendEmailResponse.success(message.getId(), sentAt);
                }
                
            } else {
                log.error("Gmail API 응답에서 메시지 ID를 찾을 수 없습니다");
                return SendEmailResponse.failure("이메일 발송에 실패했습니다");
            }
            
        } catch (Exception e) {
            log.error("Gmail을 통한 이메일 발송 중 오류 발생", e);
            return SendEmailResponse.failure("이메일 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private Gmail createGmailService(String accessToken) throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // OAuth2 액세스 토큰으로 Gmail 서비스 생성
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        
        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    private MimeMessage createEmail(String to, String from, String subject, String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        
        return email;
    }
    
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        
        Message message = new Message();
        message.setRaw(encodedEmail);
        
        return message;
    }
}